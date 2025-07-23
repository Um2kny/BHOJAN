
//javac -cp .;mysql-connector-j-9.3.0.jar dish.java
//java -cp .;mysql-connector-j-9.3.0.jar dish

import java.sql.*;
import java.io.*;
import java.util.Scanner;

public class dish {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            // Connect to DB
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/food", "root", "hk117");

            // Input fields
            System.out.print("Enter Dish Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Type (Veg/Non-Veg): ");
            String type = sc.nextLine();

            System.out.print("Enter Region: ");
            String region = sc.nextLine();

            System.out.print("Enter Ingredients (comma separated): ");
            String ingredients = sc.nextLine();

            System.out.print("Enter Quantity (e.g., 1 plate): ");
            String quantity = sc.nextLine();

            System.out.print("Enter Average Cost (in ₹): ");
            double avgCost = sc.nextDouble();

            System.out.print("Enter Time Needed (in minutes): ");
            int timeNeeded = sc.nextInt();
            sc.nextLine(); // consume leftover newline

            System.out.print("Enter Steps (comma separated): ");
            String steps = sc.nextLine();

            System.out.print("Enter Recipe By (leave blank for default 'KK'): ");
	    String recipeBy = sc.nextLine();
	    if (recipeBy.isEmpty()) {
		recipeBy = "ChatGPT";
	    }
            System.out.print("Enter Image File Name (like 001.jpg): ");
            String imageName = sc.nextLine();

            System.out.print("Enter category: ");
            String cate = sc.nextLine();

            // Prepare SQL
            String sql = "INSERT INTO recipes (name, type, region, ingredients, quantity, avg_cost, time_needed, steps,recipe_by, image,category) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?, ?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, region);
            ps.setString(4, ingredients);
            ps.setString(5, quantity);
            ps.setDouble(6, avgCost);
            ps.setInt(7, timeNeeded);
            ps.setString(8, steps);
            ps.setString(9, recipeBy);
            ps.setString(10, imageName);
            ps.setString(11, cate);

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Recipe inserted successfully!");
            }

            conn.close();
            sc.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

