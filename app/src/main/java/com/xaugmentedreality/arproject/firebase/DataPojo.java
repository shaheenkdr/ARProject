
package com.xaugmentedreality.arproject.firebase;

import java.util.List;



public class DataPojo {

    private Integer total;
    private List<Item> items = null;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }


}
