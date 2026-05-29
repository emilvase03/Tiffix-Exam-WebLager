package dk.easv.tiffixexamweblager.GUI.Controllers.components;

// Java imports
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import java.util.Optional;

public class SearchBarController {

    @FXML private TextField searchField;

    @FXML
    public void initialize() {
        // wait until parent controllers initialize() has run and set table items
        Platform.runLater(this::attachToTable);
    }

    /**
     * Walks up the scene graph from the search bar until it finds
     * an ancestor whose direct children include a TableView.
     * Works regardless of nesting depth — zero setup needed.
     */
    private void attachToTable() {
        Parent current = searchField.getParent();

        while (current != null) {
            Optional<Node> found = current.getChildrenUnmodifiable()
                    .stream()
                    .filter(n -> n instanceof TableView<?>)
                    .findFirst();

            if (found.isPresent()) {
                hookTable((TableView<?>) found.get());
                return;
            }

            current = current.getParent();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void hookTable(TableView rawTable) {
        FilteredList filtered = new FilteredList<>(rawTable.getItems(), p -> true);
        rawTable.setItems(filtered);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal == null ? "" : newVal.toLowerCase().trim();

            filtered.setPredicate(item -> {
                if (lower.isEmpty()) return true;

                for (Object colObj : rawTable.getColumns()) {
                    TableColumn col = (TableColumn) colObj;
                    if (col.getCellValueFactory() == null) continue;

                    try {
                        ObservableValue cellValue = (ObservableValue) col.getCellValueFactory()
                                .call(new TableColumn.CellDataFeatures<>(rawTable, col, item));

                        if (cellValue != null
                                && cellValue.getValue() != null
                                && cellValue.getValue().toString().toLowerCase().contains(lower)) {
                            return true;
                        }
                    } catch (Exception ignored) {}
                }
                return false;
            });
        });
    }
}