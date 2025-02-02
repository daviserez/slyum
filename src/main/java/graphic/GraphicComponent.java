package graphic;

import change.BufferCreation;
import change.Change;
import classDiagram.IDiagramComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import swing.MultiViewManager;
import swing.PanelClassDiagram;
import swing.SColorAssigner;
import swing.SPanelDiagramComponent;
import swing.Slyum;
import swing.XmlElement;
import swing.slyumCustomizedComponents.SRadioButtonMenuItem;
import utility.PersonalizedIcon;
import utility.Utility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Observable;

/**
 * Represent a graphic component in Slyum. Graphics components can't be draw itself, it's not a Swing component. It must
 * be drawed and managed by the GraphicView class. Graphic components is an abstract class containing methods and
 * attributes useful for representing a graphical component in Slyum. Mouse events, color, drawing, selectable, visible,
 * etc...
 *
 * @author David Miserez
 * @version 1.0 - 25.07.2011
 */
public abstract class GraphicComponent extends Observable implements ActionListener, XmlElement {

  public static void askNewColorForSelectedItems() {
    MultiViewManager.getSelectedGraphicView().changeColorForSelectedItems();
  }

  public static int countGraphicComponentsAssociedWith(IDiagramComponent diagramComponent) {
    return GraphicComponent.getGraphicComponentsAssociedWith(diagramComponent).size();
  }

  public static LinkedList<GraphicComponent> getGraphicComponentsAssociedWith(IDiagramComponent diagramComponent) {
    LinkedList<GraphicComponent> results = new LinkedList<>();
    for (GraphicView graphicView : MultiViewManager.getAllGraphicViews()) {
      GraphicComponent gc = graphicView.searchAssociedComponent(diagramComponent);
      if (gc != null)
        results.add(gc);
    }

    return results;
  }

  protected Point locationContextMenuRequested;
  protected JMenuItem miNewNote;
  // Save the location of the mouse uses for computing the movement or the
  // resize.
  protected Point mousePressed = new Point();

  protected GraphicView parent;
  protected boolean pictureMode = false;

  protected JPopupMenu popupMenu;
  private Color color = Color.DARK_GRAY;

  private boolean selected = false;
  private boolean visible = true;
  private boolean isLightDelete;
  private boolean highlight;

  public GraphicComponent(GraphicView parent) {
    if (parent == null)
      throw new IllegalArgumentException("parent is null");
    this.parent = parent;
    init();
  }

  /**
   * !!! This constructor is use for create the graphic view, don't use in another way !!! Graphic view is the parent
   * for all other components, but can't give itself to this constructor in its constructor...
   */
  GraphicComponent() {
    parent = (GraphicView) this;
    init();
  }

  @Override
  public void actionPerformed(
      ActionEvent e) {
    switch (e.getActionCommand()) {
      case Slyum.ACTION_NEW_NOTE_ASSOCIED:
        parent.linkNewNoteWithSelectedEntities();
        break;
      case "ColorContextMenu":
        askNewColorForSelectedItems();
        break;
      default:
        SPanelDiagramComponent.getInstance().actionPerformed(e);
        break;
    }
  }

  /**
   * Confirm ghost changes. If component have a ghost representation, call this method will translate and scale the
   * component bounds with the ghost bounds. The ghost is, usually, the representation of component during movement or
   * resize.
   *
   * @param e the mouse event
   */
  public void apply(MouseEvent e) { }

  /**
   * This method is use by the magnetic grip for computing where did it magnetize. By default, magnetic grips go to the
   * middle bounds of the component.
   *
   * @param first the current location of the magnetic grip
   * @param next the location of the next grip (the nearest grip of first grip)
   *
   * @return the {@link Point} of the anchor.
   */
  public Point computeAnchorLocation(final Point first, final Point next) {
    final Rectangle bounds = getBounds();
    return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
  }

  /**
   * Delete this component from the parent. Delete a component will delete all line associated with this component. A
   * deleted component will no longer be drawn or managed by the graphic view. This operation is irreversible. The
   * diagram component associated with will be deleted only if it doesn't exist in another view.
   */
  public void delete() {
    if (!parent.containsComponent(this)) return;

    // Unselect the component.
    setSelected(false);

    pushBufferDestruction();

    parent.removeComponent(this);

    parent.getLinesViewAssociedWith(this).stream().forEach((lv) -> {
      if (isLightDelete)
        lv.lightDelete();
      else
        lv.delete();
    });

    PanelClassDiagram.refreshHierarchicalView();
  }

  public boolean getIsLightDelete() {
    return isLightDelete;
  }

  public final void lightDelete() {
    boolean ld = isLightDelete;
    isLightDelete = true;
    delete();
    isLightDelete = ld;
  }

  public final void hardDelete() {
    boolean ld = isLightDelete;
    isLightDelete = false;
    delete();
    isLightDelete = ld;
  }

  protected void pushBufferDestruction() {
    Change.push(new BufferCreation(true, this));
    Change.push(new BufferCreation(false, this));
  }

  /**
   * When the user select a component, this method is called. It's used for drawing a selected effect, like an etched
   * border.
   *
   * @param g2 the graphic context
   */
  public void drawSelectedEffect(Graphics2D g2) {

  }

  public boolean existsInOthersViews() {
    return GraphicComponent.countGraphicComponentsAssociedWith(getAssociatedComponent()) > 1;
  }

  public boolean componentAlreadyExists() {
    return GraphicComponent.countGraphicComponentsAssociedWith(getAssociatedComponent()) > 0;
  }

  /**
   * Mouse event - this event is called by the graphic view when the user clicks on it. The graphic view make a link
   * between swing and slyum events.
   *
   * @param e the swing mouse event.
   */
  public void gMouseClicked(MouseEvent e) {

  }

  /**
   * Mouse event - this event is called by the graphic view when the user keeps the mouse pressed and move it. The
   * graphic view make a link between swing and slyum events.
   *
   * @param e the swing mouse event.
   */
  public void gMouseDragged(MouseEvent e) { }

  /**
   * Mouse event - this event is called by the graphic view when the user enters in the component with the mouse. The
   * graphic view make a link between swing and slyum events.
   *
   * @param e the swing mouse event.
   */
  public void gMouseEntered(MouseEvent e) { }

  /**
   * Mouse event - this event is called by the graphic view when the user exits from the component with the mouse. The
   * graphic view make a link between swing and slyum events.
   *
   * @param e the swing mouse event.
   */
  public void gMouseExited(MouseEvent e) { }

  /**
   * Mouse event - this event is called by the graphic view when the user moves on the component with the mouse. The
   * graphic view make a link between swing and slyum events.
   *
   * @param e the swing mouse event.
   */
  public void gMouseMoved(MouseEvent e) { }

  /**
   * Mouse event - this event is called by the graphic view when the user presses a mouse button on the component. The
   * graphic view make a link between swing and slyum events.
   *
   * @param e the swing mouse event.
   */
  public void gMousePressed(MouseEvent e) {
    maybeShowPopup(e, popupMenu);
  }

  /**
   * Mouse event - this event is called by the graphic view when the user releases a mouse button on the component. The
   * graphic view make a link between swing and slyum events.
   *
   * @param e the swing mouse event.
   */
  public void gMouseReleased(MouseEvent e) {
    maybeShowPopup(e, popupMenu);
  }

  /**
   * Some graphic components are associated with a structural UML component (like classes, methods, relations, ...).
   * Returns {@code null} if no component are associated. !!! GraphicComponent and GraphicComponent associated with UML
   * component should be separated in newer version !!!
   *
   * @return the associated {@link IDiagramComponent} components.
   */
  public IDiagramComponent getAssociatedComponent() {
    return null;
  }

  public IDiagramComponent getAssociatedXmlElement() {
    return getAssociatedComponent();
  }

  /**
   * Get the bounds of this component. The bounds is the minimum (x, y) location and the width and height is compute in
   * this way (maxX - minX) and (maxY - minY).
   *
   * @return the bounds of this component.
   */
  public abstract Rectangle getBounds();

  /**
   * Set the bounds for the component. Any component don't have rectangulare bounds and this method can have no effect
   * on them. Calls the appropriate method of sub element for changed theirs bounds.
   *
   * @param bounds the new bounds for this component
   */
  public abstract void setBounds(Rectangle bounds);

  /**
   * Get the color of this component. The color can be used by the component during drawing. But it is the
   * responsibility of the sub class to use it or not.
   *
   * @return the color of this component.
   */
  public Color getColor() {
    return new Color(color.getRGB());
  }

  /**
   * Set the color for this component. The color can be used by the component during drawing. But it is the
   * responsibility of the subclass to use it or not.
   *
   * @param rgb the color to apply.
   */
  public void setColor(final int rgb) {
    setColor(new Color(rgb));
  }

  /**
   * Set the color for this component. The color can be used by the component during drawing. But it is the
   * responsibility of the subclass to use it or not.
   *
   * @param color the new color for this component
   */
  public void setColor(final Color color) {
    this.color = new Color(color.getRGB());
    repaint();
  }

  public GraphicView getGraphicView() {
    return parent;
  }

  /**
   * Get the mouse pressed location.
   *
   * @return the mouse pressed location.
   */
  public Point getMousePressed() {
    return mousePressed;
  }

  public void setPictureMode(boolean enable) {
    pictureMode = enable;
  }

  /**
   * Get the popup menu for this component. The popup menu is shown when user make a right-click on it. Some component
   * hides this menu and don't use it.
   *
   * @return the popup menu.
   */
  public JPopupMenu getPopupMenu() {
    return popupMenu;
  }

  @Override
  public Element getXmlElement(Document doc) {
    return null;
  }

  @Override
  public String getXmlTagName() {
    return null;
  }

  /**
   * Return if the position given in parameter is on the graphic component or not. This method is used by the graphic
   * view for computing mouse event.
   *
   * @param position the position to verify
   *
   * @return true if the position is on the graphic component; false otherwise
   */
  public abstract boolean isAtPosition(Point position);

  /**
   * Return if the component is selected or not.
   *
   * @return true if the component is selected; false otherwise
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * Set the selected state for this component.
   *
   * @param selected the new selected state for this component.
   */
  public void setSelected(boolean selected) {
    if (isSelected() != selected) {
      this.selected = selected;
      repaint();
      parent.componentSelected(selected);
    }

    setChanged();
  }

  /**
   * Return if the component is visible or not.
   *
   * @return true if the component is visible; false otherwise
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Set the visible state for this component. This method repaint the component. Note that, by default, hide a
   * component means that it will no longer be drawn.
   *
   * @param visible {@code true} if this component is visible, otherwise {@code false}.
   */
  public void setVisible(final boolean visible) {
    this.visible = visible;
    repaint();
  }

  /**
   * Creates a new JMenuItem with this class like action listeners.
   *
   * @param name the name for JMenuItem
   * @param action the action command for this JMenuItem
   * @param imgIcon the icon path for the icon of this JMenuItem
   *
   * @return the new JMenuItem created
   */
  public JMenuItem makeMenuItem(String name, String action, String imgIcon) {
    final ImageIcon img = PersonalizedIcon.createImageIcon(imgIcon + ".png");

    final JMenuItem menuItem = new JMenuItem(name, img);
    menuItem.setActionCommand(action);
    menuItem.addActionListener(this);
    return menuItem;
  }

  /**
   * Makes a new JRadioButtonMenuItem with this class like action listeners.
   *
   * @param name the name for JRadioButtonMenuItem
   * @param action action the action command for this JRadioButtonMenuItem
   * @param group the group for this JRadioButtonMenuItem
   *
   * @return the new JRadioButtonMenuItem created
   */
  public JRadioButtonMenuItem makeRadioButtonMenuItem(String name, String action, ButtonGroup group) {
    final JRadioButtonMenuItem rbMenuItem = new SRadioButtonMenuItem(name);
    rbMenuItem.setActionCommand(action);
    rbMenuItem.addActionListener(this);
    group.add(rbMenuItem);

    return rbMenuItem;
  }

  /**
   * Displays the popup menu if e.isPopupTrigger is true, hide otherwise.
   *
   * @param e the mouse event
   * @param popupMenu the popupMenu to display or hide.
   */
  public void maybeShowPopup(MouseEvent e, JPopupMenu popupMenu) {
    GraphicView gv = MultiViewManager.getSelectedGraphicView();
    locationContextMenuRequested = e.getPoint();

    if (e.isPopupTrigger()) {
      miNewNote.setEnabled(getAssociatedComponent() != null);
      popupMenu.show(e.getComponent(),
                     (int) (e.getX() / gv.getInversedScale()),
                     (int) (e.getY() / gv.getInversedScale()));
    }
  }

  public void maybeShowPopup(MouseEvent e, GraphicComponent source) {
    source.locationContextMenuRequested = e.getPoint();
    maybeShowPopup(e, source.getPopupMenu());
  }

  /**
   * Compute the middle bounds of the component. Middle bounds is computed from the bounds of the component.
   *
   * @return the {@link Point} corresponding to the middle bounds.
   */
  public Point middleBounds() {
    final Rectangle bounds = getBounds();
    return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
  }

  /**
   * Calls when a component is moved. Most of the components use a ghost representation for moving. Call apply() after
   * move for applying changes.
   *
   * @param e the {@link MouseEvent}.
   */
  public void move(final MouseEvent e) { }

  /**
   * Draw the component on the graphic view.
   *
   * @param g2 The graphic context
   */
  public abstract void paintComponent(Graphics2D g2);

  /**
   * Repaint the component on the graphic view. Most of the components call getBounds() method for repaint only its
   * bounds. When a component is moved or resize, calls this method is not sufficient because it redraws only current
   * bounds, not old bounds.
   */
  public abstract void repaint();

  /**
   * Calls for resizing component from left. Not all components can be resizing. By default, component can't be resize.
   *
   * @param e the mouse event for compute new size
   */
  public void resizeLeft(MouseEvent e) { }

  /**
   * Calls for resizing component from right. Not all components can be resizing. By default, component can't be
   * resize.
   *
   * @param e the mouse event for compute new size
   */
  public void resizeRight(MouseEvent e) { }

  public void restore() {
    PanelClassDiagram.refreshHierarchicalView();
  }

  /**
   * Calls this method for saving mouse location. Mouse location is used by components for compute moving or resizing.
   *
   * @param e the mouse event
   */
  public void saveMouseLocation(MouseEvent e) {
    mousePressed = new Point(e.getPoint());
  }

  /**
   * Some component have different styles. Calls this method reset the default style for this component.
   */
  public void setDefaultStyle() { }

  /**
   * Some component have different styles. Calls this method draw the component with its mouse hover style.
   */
  public void setMouseHoverStyle() { }

  /**
   * Some component have different styles. Calls this method draw the component with its mouse clicked style.
   */
  public void setStyleClicked() { }

  protected boolean displayGeneralMenuItems() {
    return true;
  }

  public void userDelete() {
    lightDelete();
  }

  /**
   * Calls by the constructor for initialize components.
   */
  private void init() {
    // Create context menu.
    popupMenu = new JPopupMenu();

    JMenuItem menuItem;

    miNewNote = menuItem = makeMenuItem("New note",
                                        Slyum.ACTION_NEW_NOTE_ASSOCIED, "note");
    menuItem.setVisible(displayGeneralMenuItems());
    popupMenu.add(menuItem);

    menuItem = makeMenuItem("Change color...", "ColorContextMenu", "color");
    menuItem.setVisible(displayGeneralMenuItems());
    popupMenu.add(menuItem);

    menuItem = SColorAssigner.createMenuRecentColor();
    menuItem.setVisible(displayGeneralMenuItems());
    popupMenu.add(menuItem);
  }

  public String getFullString() {
    return "";
  }

  public LinkedList<? extends GraphicComponent> getDirectChilds() {
    return new LinkedList<>();
  }

  public final LinkedList<GraphicComponent> getChildsRecursively() {

    LinkedList<GraphicComponent> allChilds = (LinkedList<GraphicComponent>) getDirectChilds();

    getDirectChilds().stream().forEach((child) -> {
      allChilds.addAll(child.getChildsRecursively());
    });

    return allChilds;
  }

  public void setHighlight(boolean highlight) {
    this.highlight = highlight;

    if (highlight)
      centerOnScreen();

    repaint();
  }

  public void centerOnScreen() {
    JViewport viewPort = parent.getScrollPane().getViewport();

    Rectangle viewPortBounds = viewPort.getBounds();
    Rectangle componentBounds = Utility.scaleRect(getBounds(), parent.getScale());

    Rectangle centerRect = new Rectangle(new Point(
        componentBounds.x + (componentBounds.width / 2) - (viewPortBounds.width / 2),
        componentBounds.y + (componentBounds.height / 2) - (viewPortBounds.height / 2)));

    Point p = centerRect.getLocation();
    parent.getScrollPane().getViewport().setViewPosition(
        new Point(p.x < 0 ? 0 : p.x, p.y < 0 ? 0 : p.y));
  }

  public boolean isHighlight() {
    return highlight;
  }

  public static void removeHighlightForAllComponents() {
    MultiViewManager.getAllGraphicViews().stream().forEach((gv) -> {
      gv.getChildsRecursively().stream().forEach((gc) -> {
        gc.setHighlight(false);
      });
    });
  }

}
