// Compile: javac -cp .;mysql-connector-j-9.3.0.jar bhojan.java
// Run:     java -cp .;mysql-connector-j-9.3.0.jar bhojan
import java.io.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class bhojan extends JFrame {
    JPanel fp, cp, rp;
    JSplitPane splitPane;
    JScrollPane scrollPane;
    JTextField searchField;
    List<Recipe> allRecipes = new ArrayList<>();
    Set<String> selectedTypes = new HashSet<>();
    Set<String> selectedRegions = new HashSet<>();
    Set<String> selectedIngredients = new HashSet<>();
    //Set<String> selectedCostRanges = new HashSet<>(); 
    Set<String> selectedTimeRanges = new HashSet<>();
Set<String> selectedCategories = new HashSet<>();
    Color chipColor = new Color(240, 234, 214);
    Color selectedColor = new Color(255, 171, 90);
    Color goldenOrange = new Color(244, 162, 97);
    int currentPage = 1, pageSize = 10;
    JPanel paginationPanel;

    public bhojan() {
    try {
        setTitle("BHOJAN");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 720));
        setExtendedState(MAXIMIZED_BOTH);
        setIconImage(new ImageIcon("img/icon.png").getImage());
        setVisible(true);

        // Load Recipes before Filters
        loadRecipes();

        fp = new JPanel();
        fp.setLayout(new BoxLayout(fp, BoxLayout.Y_AXIS));
        fp.setBackground(new Color(245, 228, 195));
        fp.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JScrollPane filterScrollPane = new JScrollPane(fp);
        filterScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        filterScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Build Filters after loading recipes
        addFilterSection("Type", List.of("Veg", "Non-Veg"), selectedTypes);
        addFilterSection("Region", extractUnique("region"), selectedRegions);
        addFilterSection("Category", extractUnique("category"), selectedCategories);
        addFilterSection("Ingredients", extractUniqueIngredients(), selectedIngredients);
        addFilterSection("Time (min)", List.of("<10", "0-60", ">60"), selectedTimeRanges);

        // Search Bar Section
        JPanel sp = new JPanel();
        sp.setLayout(new BoxLayout(sp, BoxLayout.Y_AXIS));
        sp.setBackground(new Color(255, 111, 97));
        JLabel title = new JLabel("BHOJAN");
        title.setFont(new Font("Serif", Font.BOLD, 80));
        title.setForeground(new Color(80, 20, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sp.add(title);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchRow.setBackground(Color.WHITE);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Serif", Font.PLAIN, 30));
        searchField = new JTextField(30);
        searchField.setFont(new Font("Serif", Font.PLAIN, 24));
        searchRow.add(searchLabel);
        searchRow.add(searchField);
        sp.add(searchRow);

        // Recipe Cards Panel
        cp = new JPanel();
        cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));
        cp.setBackground(Color.WHITE);
        scrollPane = new JScrollPane(cp);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        rp = new JPanel(new BorderLayout());
        rp.add(sp, BorderLayout.NORTH);
        rp.add(scrollPane, BorderLayout.CENTER);
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rp.add(paginationPanel, BorderLayout.SOUTH);
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filterScrollPane, rp);
        splitPane.setDividerLocation(350);
        splitPane.setDividerSize(1);
        add(splitPane);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshCards(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshCards(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshCards(); }
        });

        refreshCards();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
    }
}

void addFilterSection(String title, List<String> items, Set<String> selectedSet) {
    JPanel section = new JPanel(new BorderLayout());
    section.setBackground(fp.getBackground());

    JLabel heading = new JLabel(title);
    heading.setFont(new Font("Segoe UI", Font.BOLD, 16));
    heading.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    JPanel chipPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 6));
    chipPanel.setBackground(fp.getBackground());

    for (String item : items) {
        JLabel chip = createChip(item, selectedSet);
        chipPanel.add(chip);
    }

    JScrollPane chipScroll = new JScrollPane(chipPanel);
    chipScroll.setBorder(null);
    chipScroll.setBackground(fp.getBackground());
    chipScroll.getViewport().setBackground(fp.getBackground());
    chipScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    // Enable vertical scrollbar if many chips
    chipScroll.setVerticalScrollBarPolicy(
        items.size() > 10 ? JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED : JScrollPane.VERTICAL_SCROLLBAR_NEVER
    );

    int height = (items.size() > 10) ? 130 : ((items.size() / 4 + 1) * 36);
    chipScroll.setPreferredSize(new Dimension(300, height));

    section.add(heading, BorderLayout.NORTH);
    section.add(chipScroll, BorderLayout.CENTER);
    section.setAlignmentX(Component.LEFT_ALIGNMENT);
    fp.add(Box.createVerticalStrut(4));
    fp.add(section);
}


    JLabel createChip(String label, Set<String> selectedSet) {
        JLabel chip = new JLabel(label);
        chip.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chip.setOpaque(true);
        chip.setBackground(chipColor);
        chip.setForeground(Color.DARK_GRAY);
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.setHorizontalAlignment(SwingConstants.CENTER);
        chip.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(20),
                BorderFactory.createEmptyBorder(0, 6, 0, 6)));

        chip.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (selectedSet.contains(label)) {
                    selectedSet.remove(label);
                    chip.setBackground(chipColor);
                } else {
                    selectedSet.add(label);
                    chip.setBackground(selectedColor);
                }
                refreshCards();
            }
        });
        return chip;
    }

    void refreshCards() {
        cp.removeAll();
        String query = searchField.getText().toLowerCase().trim();

        List<Recipe> filtered = allRecipes.stream()
                .filter(r -> selectedTypes.isEmpty() || selectedTypes.contains(r.type))
                .filter(r -> selectedRegions.isEmpty() || selectedRegions.contains(r.region))
.filter(r -> selectedCategories.isEmpty() || selectedCategories.contains(r.category))
		.filter(r -> selectedIngredients.isEmpty() || 
		    Arrays.stream(r.ingredients.split(":"))
		          .map(String::trim)
		          .map(String::toLowerCase)
		          .anyMatch(i -> selectedIngredients.stream()
        		  		    .map(String::toLowerCase)
                                            .collect(Collectors.toSet())
                                            .contains(i)))
                //.filter(r -> selectedCostRanges.isEmpty() || matchRange(r.avgCost, selectedCostRanges))
                .filter(r -> selectedTimeRanges.isEmpty() || matchRange(r.timeNeeded, selectedTimeRanges))
                .filter(r -> query.isEmpty() || r.name.toLowerCase().contains(query))
                //.forEach(r -> cp.add(r.createCard()));
		.collect(Collectors.toList());

        int totalPage = (int)Math.ceil(filtered.size() / (double)pageSize);
        if (currentPage > totalPage) currentPage = 1;

        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, filtered.size());
        List<Recipe> pageData = filtered.subList(start, end);

        for (Recipe r : pageData) {
            cp.add(r.createCard());
            cp.add(Box.createVerticalStrut(10));
        }
        cp.revalidate();
        cp.repaint();
        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null && scrollPane.getVerticalScrollBar() != null) {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });
	updatePagination(totalPage);
    }

    boolean matchRange(int val, Set<String> ranges) {
        for (String r : ranges) {
            if (r.equals("<10") && val < 10) return true;
            if (r.equals("0-60") && val >= 0 && val <= 60) return true;
            if (r.equals(">60") && val > 60) return true;
        }
        return false;
    }
void updatePagination(int totalPage) {
    paginationPanel.removeAll();

    JButton first = new JButton("<<");
    first.addActionListener(e -> { currentPage = 1; refreshCards(); });
    first.setEnabled(currentPage != 1);
    paginationPanel.add(first);

    JButton prev = new JButton("<");
    prev.addActionListener(e -> { if (currentPage > 1) currentPage--; refreshCards(); });
    prev.setEnabled(currentPage > 1);
    paginationPanel.add(prev);

    int visiblePages = 5;
    int startPage = Math.max(1, currentPage - 2);
    int endPage = Math.min(totalPage, currentPage + 2);

    if (startPage > 1) {
        paginationPanel.add(createPageButton(1));
        if (startPage > 2) paginationPanel.add(new JLabel("..."));
    }

    for (int i = startPage; i <= endPage; i++) {
        JButton btn = new JButton(String.valueOf(i));
        if (i == currentPage) {
            btn.setEnabled(false);
        }
        final int page = i;
        btn.addActionListener(e -> { currentPage = page; refreshCards(); });
        paginationPanel.add(btn);
    }

    if (endPage < totalPage) {
        if (endPage < totalPage - 1) paginationPanel.add(new JLabel("..."));
        paginationPanel.add(createPageButton(totalPage));
    }

    JButton next = new JButton(">");
    next.addActionListener(e -> { if (currentPage < totalPage) currentPage++; refreshCards(); });
    next.setEnabled(currentPage < totalPage);
    paginationPanel.add(next);

    JButton last = new JButton(">>");
    last.addActionListener(e -> { currentPage = totalPage; refreshCards(); });
    last.setEnabled(currentPage != totalPage);
    paginationPanel.add(last);

    paginationPanel.revalidate();
    paginationPanel.repaint();
}

private JButton createPageButton(int pageNumber) {
    JButton btn = new JButton(String.valueOf(pageNumber));
    if (pageNumber == currentPage) btn.setEnabled(false);
    btn.addActionListener(e -> {
        currentPage = pageNumber;
        refreshCards();
    });
    return btn;
}
void loadRecipes() {
    allRecipes.clear(); 
    try (BufferedReader br = new BufferedReader(new FileReader("data/d.csv"))) {
        String line;
        br.readLine(); // Skip header line
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\|");
            if (parts.length < 12) continue; // Skip invalid lines
            allRecipes.add(new Recipe(parts));
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}


List<String> extractUnique(String field) {
    return allRecipes.stream()
            .map(r -> {
                if (field.equals("region")) return r.region;
                else if (field.equals("category")) return r.category;
                else return "";
            })
            .filter(s -> !s.isBlank())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
}

List<String> extractUniqueIngredients() {
    return allRecipes.stream()
            .flatMap(r -> Arrays.stream(r.ingredients.split(":")))  // Changed to comma
            .map(String::trim)
            .map(String::toLowerCase)
            .filter(s -> !s.isBlank())
            .distinct()
            .sorted()
            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
            .collect(Collectors.toList());
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(bhojan::new);
    }

    class Recipe {
        String name, type, region, ingredients, steps, image, quantity, recipe_by, category;
        int avgCost, timeNeeded;
Recipe(String[] parts) {
    name = parts[1];
    type = parts[2];
    region = parts[3];
    ingredients = parts[4];
    quantity = parts[5];
    avgCost = Integer.parseInt(parts[6]);
    timeNeeded = Integer.parseInt(parts[7]);
    steps = parts[8];
    recipe_by = parts[9];
    image = parts[10];
    category = parts[11];
}

        JPanel createCard() {
            JPanel card = new JPanel(new BorderLayout());
            card.setPreferredSize(new Dimension(300, 400));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
            card.setBackground(goldenOrange);
            card.setBorder(BorderFactory.createMatteBorder(2, 0, 2, 2, Color.BLACK));

            JPanel infoPanel = new JPanel(new GridLayout(0, 2, 6, 4));
            infoPanel.setOpaque(false);
            infoPanel.add(makeInfoLabel("Name: " + name));
            infoPanel.add(makeInfoLabel("Type: " + category));
            infoPanel.add(makeInfoLabel("Region: " + region));
            infoPanel.add(makeInfoLabel("Serving: " + quantity));
            infoPanel.add(makeInfoLabel("Cost: ₹" + avgCost));
            infoPanel.add(makeInfoLabel("Time: " + timeNeeded + " min"));
            infoPanel.add(makeInfoLabel("Steps: " + steps.split(";").length));
            infoPanel.add(makeInfoLabel("Recipe By: " + recipe_by));

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
            ImageIcon icon = new ImageIcon("img/" + image);
            Image img = icon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));

            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.setOpaque(false);
            imagePanel.add(imageLabel, BorderLayout.CENTER);
            imagePanel.setPreferredSize(new Dimension(220, 220));

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setOpaque(false);
            mainPanel.add(imagePanel, BorderLayout.WEST);
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            card.add(mainPanel, BorderLayout.CENTER);

            JPanel accent = new JPanel();
            accent.setPreferredSize(new Dimension(20, 140));
            accent.setBackground(type.equalsIgnoreCase("Veg") ? Color.GREEN : Color.RED);
            card.add(accent, BorderLayout.WEST);

            JPanel stepsPanel = new JPanel();
            stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.Y_AXIS));
            stepsPanel.setOpaque(false);
            stepsPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 10));

            JLabel heading = new JLabel("Procedure");
            heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
            stepsPanel.add(heading);

            String[] stepList = steps.split(";");
            for (int i = 0; i < stepList.length; i++) {
                JTextArea stepLabel = new JTextArea((i + 1) + ". " + stepList[i].trim());
                stepLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17));
                stepLabel.setOpaque(false);
                stepLabel.setEditable(false);
                stepLabel.setLineWrap(true);
                stepLabel.setWrapStyleWord(true);
                stepsPanel.add(stepLabel);
            }

            JScrollPane stepScroll = new JScrollPane(stepsPanel);
            stepScroll.setPreferredSize(new Dimension(0, 200));
            stepScroll.setOpaque(false);
            stepScroll.getViewport().setOpaque(false);
            stepScroll.setBorder(null);
            stepScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            stepScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            SwingUtilities.invokeLater(() -> stepScroll.getVerticalScrollBar().setValue(0));
            card.add(stepScroll, BorderLayout.SOUTH);

            return card;
        }

        private JLabel makeInfoLabel(String text) {
            JLabel label = new JLabel(text);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            return label;
        }
    }

    class RoundedBorder extends AbstractBorder {
        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.GRAY);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(radius / 2, radius / 2, radius / 2, radius / 2);
            return insets;
        }
    }
} 