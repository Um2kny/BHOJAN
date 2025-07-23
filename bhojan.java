//javac -cp .;mysql-connector-j-9.3.0.jar bhojan.java
//java -cp .;mysql-connector-j-9.3.0.jar bhojan

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Arrays;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class bhojan extends JFrame {
    JPanel fp, sp, cp, rp;
    JSplitPane splitPane;
    Color warmBeige = new Color(245, 228, 195);
    Color lightTomato = new Color(255, 111, 97);
    Color goldenOrange = new Color(244, 162, 97);
    Color softCream = new Color(255, 247, 230);
    Color burntRed = new Color(231, 111, 81);

    public bhojan() {
        try {
            int initialWidth = 700;
            int initialHeight = 600;
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/food", "root", "hk117");

            // Get recipe count
            PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM recipes");
            ResultSet rs = pst.executeQuery();
            int recordCount = rs.next() ? rs.getInt(1) : 0;
            rs.close();
            pst.close();

            // Create menu array
            String[] menu = new String[recordCount];
            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM recipes");
            ResultSet rst = stmt.executeQuery();
            for (int i = 0; i < recordCount && rst.next(); i++) {
                menu[i] = rst.getString("name");
            }
            rst.close();
            stmt.close();

            // Initial copy
            String[] safai = Arrays.copyOf(menu, menu.length);

            // GUI panels
            fp = new JPanel();
            fp.setBackground(warmBeige);
            fp.setPreferredSize(new Dimension((int) (initialWidth * 0.30), initialHeight));
            fp.add(new JLabel("Filters here"));

            sp = new JPanel();
            sp.setLayout(new BoxLayout(sp, BoxLayout.Y_AXIS));
            sp.setBackground(lightTomato);

            JLabel titleLabel = new JLabel("BHOJAN 50");
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            titleLabel.setFont(new Font("Serif", Font.BOLD, 80));
            titleLabel.setForeground(new Color(80, 20, 0));
            sp.add(titleLabel);

            JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            searchRow.setBackground(Color.WHITE);
            searchRow.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JLabel searchLabel = new JLabel("Search:");
            searchLabel.setFont(new Font("Serif", Font.PLAIN, 30));
            JTextField searchField = new JTextField(30);
            searchField.setFont(new Font("Serif", Font.PLAIN, 24));
            searchRow.add(searchLabel);
            searchRow.add(searchField);
            sp.add(searchRow);

            cp = new JPanel();
            cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));
            cp.setBackground(Color.WHITE);

            // Search logic
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                void filterRecipes() {
                    cp.removeAll();
                    String t = searchField.getText().trim().toLowerCase();
                    String[] filtered;

                    if (t.isEmpty()) {
                        filtered = Arrays.copyOf(menu, menu.length);
                    } else {
                        filtered = Arrays.stream(menu)
                                .filter(name -> name.toLowerCase().contains(t))
                                .toArray(String[]::new);
                    }

                    for (String dn : filtered) {
                        try {
                            PreparedStatement p = conn.prepareStatement("SELECT * FROM recipes WHERE name = ?");
                            p.setString(1, dn);
                            ResultSet r = p.executeQuery();

                            if (r.next()) {
                                JPanel card = createCard(r);
                                cp.add(card);
                                cp.add(Box.createRigidArea(new Dimension(0, 20)));
                            }
                            r.close();
                            p.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    cp.revalidate();
                    cp.repaint();
                }

                public void insertUpdate(DocumentEvent e) { filterRecipes(); }
                public void removeUpdate(DocumentEvent e) { filterRecipes(); }
                public void changedUpdate(DocumentEvent e) { filterRecipes(); }
            });

            // Initial card load
            for (String dn : safai) {
                PreparedStatement p = conn.prepareStatement("SELECT * FROM recipes WHERE name = ?");
                p.setString(1, dn);
                ResultSet r = p.executeQuery();

                if (r.next()) {
                    JPanel card = createCard(r);
                    cp.add(card);
                    cp.add(Box.createRigidArea(new Dimension(0, 20)));
                }
                r.close();
                p.close();
            }

            JScrollPane scrollPane = new JScrollPane(cp);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.setBorder(null);

            rp = new JPanel(new BorderLayout());
            rp.add(sp, BorderLayout.NORTH);
            rp.add(scrollPane, BorderLayout.CENTER);
	    SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));

            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fp, rp);
            splitPane.setDividerLocation((int) (initialWidth * 0.30));
            splitPane.setEnabled(false);
            splitPane.setDividerSize(1);

            add(splitPane);

            setTitle("BHOJAN");
            setSize(initialWidth, initialHeight);
            setLocation(350, 100);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
            setResizable(true);

            addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    splitPane.setDividerLocation((int) (getWidth() * 0.30));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createCard(ResultSet r) throws SQLException {
        String dishName = r.getString("name");
        String region = r.getString("region");
        String cat = r.getString("type");
        String ingredients = r.getString("ingredients");
        String qty = r.getString("quantity");
        String cost = r.getString("avg_cost");
        String time = r.getString("time_needed");
        String imageName = r.getString("image");
        String steps = r.getString("steps");
        String creatorName = r.getString("recipe_by");
        String cname = r.getString("category");
        int stepCount = steps.split(";").length;

        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(300, 400));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        card.setBackground(goldenOrange);
        card.setBorder(BorderFactory.createMatteBorder(2, 0, 2, 2, Color.BLACK));

        // Info
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 6, 4));
        infoPanel.setOpaque(false);
        infoPanel.add(makeInfoLabel("Name: " + dishName));
        infoPanel.add(makeInfoLabel("Type: " + cname));
        infoPanel.add(makeInfoLabel("Region: " + region));
        infoPanel.add(makeInfoLabel("Serving: " + qty));
        infoPanel.add(makeInfoLabel("Cost: ₹" + cost));
        infoPanel.add(makeInfoLabel("Time: " + time + " min"));
        infoPanel.add(makeInfoLabel("Steps: " + stepCount));
        infoPanel.add(makeInfoLabel("Recipe By: " + creatorName));

        JTextArea ingArea = new JTextArea("Ingredients: " + ingredients);
        ingArea.setLineWrap(true);
        ingArea.setWrapStyleWord(true);
        ingArea.setOpaque(false);
        ingArea.setEditable(false);
        ingArea.setFont(new Font("Segoe UI", Font.PLAIN, 17));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        centerPanel.add(infoPanel, BorderLayout.CENTER);
        centerPanel.add(ingArea, BorderLayout.SOUTH);

        JLabel imageLabel = new JLabel();
        ImageIcon icon = new ImageIcon("C:/Users/kansa/OneDrive/Desktop/java/bhojan/img/" + imageName);
        Image img = icon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(img));

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setOpaque(false);
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        imagePanel.setPreferredSize(new Dimension(220, 220));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setPreferredSize(new Dimension(200, 220));
        mainPanel.add(imagePanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        card.add(mainPanel, BorderLayout.CENTER);

        JPanel accent = new JPanel();
        accent.setPreferredSize(new Dimension(20, 140));
        accent.setBackground(cat.equalsIgnoreCase("Veg") ? Color.GREEN : Color.RED);
        card.add(accent, BorderLayout.WEST);

        String[] sl = steps.split(";");
        JPanel stepsPanel = new JPanel();
        stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.Y_AXIS));
        stepsPanel.setOpaque(false);
        stepsPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 10));

						JLabel heading = new JLabel("Procedure:");
						heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
						stepsPanel.add(heading);
        for (int k = 0; k < sl.length; k++) {
            JTextArea stepLabel = new JTextArea((k + 1) + ". " + sl[k].trim());
            stepLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            stepLabel.setOpaque(false);
            stepLabel.setEditable(false);
            stepLabel.setLineWrap(true);
            stepLabel.setWrapStyleWord(true);
            stepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            stepsPanel.add(stepLabel);
        }

        JScrollPane stepScroll = new JScrollPane(stepsPanel);
        stepScroll.setPreferredSize(new Dimension(0, 200));
        stepScroll.setOpaque(false);
        stepScroll.getViewport().setOpaque(false);
        stepScroll.setBorder(null);
	stepScroll.getVerticalScrollBar().setValue(0);
        stepScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        stepScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        card.add(stepScroll, BorderLayout.SOUTH);
	SwingUtilities.invokeLater(() -> stepScroll.getVerticalScrollBar().setValue(0));
        return card;
    }

    private JLabel makeInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(bhojan::new);
    }
}
