package swing;

import change.Change;
import classDiagram.ClassDiagram;
import graphic.GraphicView;
import utility.PersonalizedIcon;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

public class STab extends JTabbedPane {

  private static STab instance;
  private int saveCurrentSelectedIndex;

  public static void initialize(GraphicView graphicView) {
    instance = new STab(graphicView);
  }

  public static STab getInstance() {
    if (instance == null)
      throw new IllegalArgumentException(
          "STab must be initialized before called. Use STab.initialize().");
    return instance;
  }

  private STab(GraphicView graphicView) {

    // Add main tab.
    super.add(GraphicView.ROOT_VIEW_DEFAULT_NAME, graphicView.getScrollPane());
    saveCurrentSelectedIndex = 0;

    setTabComponentAt(0, new GraphicViewTabComponent(this, graphicView));
    graphicView.getClassDiagram().addObserver(
        (GraphicViewTabComponent) getTabComponentAt(0));

    // Add + tab.
    addPlusTab();
    setSelectedIndex(0);

    setTabLayoutPolicy(SCROLL_TAB_LAYOUT);

    addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            STab source = (STab) e.getSource();

            if (source.getTabCount() <= saveCurrentSelectedIndex)
              return;

            GraphicViewTabComponent
                gvtcPrevious = STab.this.getTabComponentAt(saveCurrentSelectedIndex),
                gvtcCurrent = source.getTabComponentAt(source.getSelectedIndex());

            if (gvtcPrevious != null && gvtcCurrent != null) {
              tabChanged(gvtcCurrent.getGraphicView(), gvtcPrevious.getGraphicView());
              saveCurrentSelectedIndex = ((STab) e.getSource()).getSelectedIndex();
            } else if (gvtcPrevious != null) {
              setSelectedIndex(saveCurrentSelectedIndex);
            }
          }
        });
      }
    });

    addMouseListener(new MouseAdapter() {

      @Override
      public void mousePressed(MouseEvent e) {
        if (getBoundsAt(getTabCount() - 1).contains(e.getPoint()))
          if (MultiViewManager.getAllClosedGraphicViews().isEmpty())
            MultiViewManager.addAndOpenNewView();
          else
            NewViewDialog.displayDialog();
      }
    });

    setUI(new BasicTabbedPaneUI() {

      @Override
      protected int calculateTabWidth(
          int tabPlacement, int tabIndex, FontMetrics metrics) {
        if (tabIndex == getTabCount() - 1) // +
          return 25;
        return 150;
      }

      @Override
      protected int calculateTabHeight(
          int tabPlacement, int tabIndex, int fontHeight) {
        return 30;
      }

      @Override
      protected int calculateMaxTabWidth(int tabPlacement) {
        return 250;
      }

      @Override
      protected void paintTabBackground(
          Graphics g, int tabPlacement, int tabIndex,
          int x, int y, int w, int h, boolean isSelected) {
        if (isSelected)
          g.setColor(graphic.GraphicView.getBasicColor());
        else
          g.setColor(graphic.GraphicView.getBasicColor().equals(Color.WHITE) ?
                         new Color(220, 220, 220) : Color.WHITE);

        g.fillRect(x, y, w, h - 1);
      }

      @Override
      protected void paintTabBorder(
          Graphics g, int tabPlacement, int tabIndex,
          int x, int y, int w, int h, boolean isSelected) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1.0f));

        g.setColor(Slyum.DISABLE_COLOR);
        g.drawLine(x, y, x, y + h - 2); // left
        g.drawLine(x, y, x + w - 1, y); // top
        g.drawLine(x + w, y, x + w, y + h - 2); // right

        g2.setStroke(new BasicStroke(2.0f));
        if (isSelected) {
          g2.setColor(Slyum.THEME_COLOR);
          g2.drawLine(x + 1, y, x + w, y);
        }
      }

      @Override
      protected void paintFocusIndicator(
          Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
          Rectangle iconRect, Rectangle textRect, boolean isSelected) { }

      @Override
      protected void paintContentBorderTopEdge(
          Graphics g, int tabPlacement, int selectedIndex,
          int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2.0f));
        g2.setColor(Slyum.THEME_COLOR);
        g2.drawLine(x, y, x + w, y);
      }

      @Override
      protected void paintContentBorderBottomEdge(
          Graphics g, int tabPlacement, int selectedIndex,
          int x, int y, int w, int h) { }

      @Override
      protected void paintContentBorderLeftEdge(
          Graphics g, int tabPlacement, int selectedIndex,
          int x, int y, int w, int h) { }

      @Override
      protected void paintContentBorderRightEdge(
          Graphics g, int tabPlacement, int selectedIndex,
          int x, int y, int w, int h) { }

      @Override
      protected Insets getContentBorderInsets(int tabPlacement) {
        return new Insets(-1, 0, 0, 0);
      }
    });
  }

  public void tabChanged(GraphicView currentGraphicView,
                         GraphicView previousGraphicView) {
    MultiViewManager.setSelectedGraphicView(currentGraphicView);
    currentGraphicView.unselectAll();
    currentGraphicView.refreshAllComponents();
    Change.checkToolbarButtonState();
  }

  public void openTab(GraphicView graphicView) {
    addClosableTab(graphicView);
  }

  private void addClosableTab(GraphicView graphicView) {
    // Remove the "+" tab for adding a new and re-adding the "+" tab
    // to put it at the end.
    int tabCount = getTabCount();
    remove(tabCount - 1);

    super.addTab(graphicView.getName(), graphicView.getScrollPane());
    setTabComponentAt(getTabCount() - 1, new ClosableTitleTab(this, graphicView));
    addPlusTab();
    setSelectedIndex(tabCount - 1);
  }

  @Override
  public void addTab(String title, Icon icon, Component component) { }

  @Override
  public void addTab(
      String title, Icon icon, Component component, String tip) { }

  private void addPlusTab() {
    super.addTab(
        "",
        PersonalizedIcon.createImageIcon("plus-16.png"),
        null);
  }

  @Override
  public GraphicViewTabComponent getTabComponentAt(int index) {
    return (GraphicViewTabComponent) super.getTabComponentAt(index);
  }

  public LinkedList<GraphicView> getAllGraphicsView() {
    LinkedList<GraphicView> results = new LinkedList<>();
    for (int i = 0; i < getTabCount() - 1; i++)
      results.add(getTabComponentAt(i).getGraphicView());
    return results;
  }

  public static class GraphicViewTabComponent extends JPanel implements Observer {

    private GraphicView graphicView;

    protected JTabbedPane pane;
    protected JLabel label;

    public GraphicViewTabComponent(final JTabbedPane pane, GraphicView graphicView) {
      super(new BorderLayout());
      this.pane = pane;
      this.graphicView = graphicView;
      setOpaque(false);
      setPreferredSize(new Dimension(130, 30));

      add(label = new JLabel() {

        @Override
        public String getText() {
          int i = pane.indexOfTabComponent(GraphicViewTabComponent.this);
          if (i != -1) {
            return pane.getTitleAt(i);
          }
          return null;
        }
      }, BorderLayout.WEST);
      label.setPreferredSize(new Dimension(130, 30));
    }

    @Override
    public void update(Observable o, Object arg) {
      int i = pane.indexOfTabComponent(GraphicViewTabComponent.this);
      if (o instanceof ClassDiagram) {
        String text = ((ClassDiagram) o).getName();
        if (text.isEmpty())
          text = GraphicView.ROOT_VIEW_DEFAULT_NAME;
        pane.setTitleAt(i, text);
        label.setText(text);
      }
    }

    public GraphicView getGraphicView() {
      return graphicView;
    }

  }

  static class ClosableTitleTab extends GraphicViewTabComponent {
    public ClosableTitleTab(final JTabbedPane pane, GraphicView graphicView) {
      super(pane, graphicView);
      label.setPreferredSize(new Dimension(110, 30));
      add(new LabelClose(), BorderLayout.EAST);
      graphicView.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
      int i = pane.indexOfComponent(getGraphicView().getScrollPane());
      if (o instanceof GraphicView) {
        String text = ((GraphicView) o).getName();
        pane.setTitleAt(i, text);
        label.setText(text);
      }
    }

    private class LabelClose extends JLabel {

      private boolean mouseHover, mousePressed;

      public LabelClose() {
        super("");
        setPreferredSize(new Dimension(20, 20));
        addMouseListener(new MouseAdapter() {

          @Override
          public void mouseEntered(MouseEvent e) {
            mouseHover = true;
            repaint();
          }

          @Override
          public void mouseExited(MouseEvent e) {
            mouseHover = false;
            repaint();
          }

          @Override
          public void mouseClicked(MouseEvent e) {
            MultiViewManager.closeView(getGraphicView());
            repaint();
          }

          @Override
          public void mousePressed(MouseEvent e) {
            mousePressed = true;
            repaint();
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            mousePressed = false;
            repaint();
          }
        });
      }

      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Rectangle bounds = getBounds();
        int size = 6,
            x = bounds.width - size * 2,
            y = 12;

        utility.Utility.setRenderQuality(g);
        super.paintComponent(g);
        g2.setStroke(new BasicStroke(1.0f));

        if (mouseHover) {
          if (mousePressed)
            g2.setColor(new Color(223, 59, 59));
          else
            g2.setColor(new Color(254, 118, 118));

          g2.fillOval(x - 3, y - 3, size + 7, size + 7);
          g2.setColor(Color.WHITE);
        } else {
          g2.setColor(Color.DARK_GRAY);
        }

        g2.drawLine(x, y, x + size, y + size);
        g2.drawLine(x + size, y, x, y + size);
      }

    }

  }

}

