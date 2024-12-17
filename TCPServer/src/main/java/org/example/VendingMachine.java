package org.example;

import java.util.HashMap;
import java.util.Map;

class VendingMachine{
    private Map<String, Integer> products;
    private Map<String, Double> prices;
    public VendingMachine(){
        products= new HashMap<String, Integer>();
        prices= new HashMap<String, Double>();

        addProduct("Water", 10, 0.5);
        addProduct("Brownie", 10, 3);
        addProduct("Snickers", 10, 5.5);
        addProduct("Cola", 10, 4);
        addProduct("Soda", 10, 1.5);
        addProduct("Sprite", 10, 4);
        addProduct("Banana", 10, 7);
        addProduct("Apple", 10, 3);
        addProduct("Biscuit", 10, 2);
        addProduct("Chips", 10, 2.5);
        addProduct("Crunch", 10, 2);
    }

    private void addProduct(String name, int count, double price){
        products.put(name,count);
        prices.put(name,price);
    }

    public String buyProduct(String productName, double money){
        if(products.containsKey(productName) && products.get(productName) > 0 ){
            if(money - prices.get(productName) < 0){
                return "Budget is not enough to buy " + productName + "\n";
            }
            products.put(productName, products.get(productName) -1);
            return "Purchased: " + productName + ". Remaining money: " + (money - prices.get(productName)) +"\n";
        }else{
            return "Product out of stock or does not exist.\n";
        }
    }
    public void refill(){
        for(String product : products.keySet()){
            products.replace(product,10);
        }
    }

    public String displayProducts() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available products:\n");
        for (String product : products.keySet()) {
            sb.append(product).append(" - $").append(prices.get(product)).append(" (").append(products.get(product)).append(" in stock)\n");
        }
        return sb.toString();
    }
}