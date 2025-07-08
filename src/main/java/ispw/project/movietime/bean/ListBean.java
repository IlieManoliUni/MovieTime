package ispw.project.movietime.bean;

import ispw.project.movietime.model.ListModel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class ListBean {

    private final IntegerProperty id;
    private final StringProperty listName;
    private final StringProperty ownerUsername;

    private final StringProperty listNameError;

    private static final int MIN_LISTNAME_LENGTH = 3;
    private static final int MAX_LISTNAME_LENGTH = 30;

    public ListBean() {
        this.id = new SimpleIntegerProperty();
        this.listName = new SimpleStringProperty();
        this.ownerUsername = new SimpleStringProperty();
        this.listNameError = new SimpleStringProperty(); // Initialize error property
    }

    public ListBean(ListModel listModel) {
        this.id = new SimpleIntegerProperty(listModel.getId());
        this.listName = new SimpleStringProperty(listModel.getName());
        this.ownerUsername = new SimpleStringProperty(listModel.getUsername());
        this.listNameError = new SimpleStringProperty(); // Initialize for existing models too
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty listNameProperty() {
        return listName;
    }

    public StringProperty ownerUsernameProperty() {
        return ownerUsername;
    }

    public StringProperty listNameErrorProperty() {
        return listNameError;
    }


    public int getId() {
        return id.get();
    }

    public String getListName() {
        return listName.get();
    }

    public String getOwnerUsername() {
        return ownerUsername.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public void setListName(String listName) {
        this.listName.set(listName);
        validateListName();
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername.set(ownerUsername);
    }

    private boolean validateListName() {
        String currentListName = getListName();
        if (currentListName == null || currentListName.trim().isEmpty()) {
            listNameError.set("List name cannot be empty.");
            return false;
        }
        if (currentListName.trim().length() < MIN_LISTNAME_LENGTH) {
            listNameError.set("List name must be at least " + MIN_LISTNAME_LENGTH + " characters.");
            return false;
        }
        if (currentListName.trim().length() > MAX_LISTNAME_LENGTH) {
            listNameError.set("List name cannot exceed " + MAX_LISTNAME_LENGTH + " characters.");
            return false;
        }
        listNameError.set("");
        return true;
    }

    public boolean isValid() {
        return validateListName();
    }

    @Override
    public String toString() {
        return "ListBean{" +
                "id=" + id.get() +
                ", listName='" + listName.get() + '\'' +
                ", ownerUsername='" + ownerUsername.get() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListBean listBean = (ListBean) o;
        // Comparing by ID is usually sufficient for equality in lists of beans
        return id.get() == listBean.id.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }
}