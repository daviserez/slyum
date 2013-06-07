package graphic.entity;

import graphic.ColoredComponent;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.GraphicView.ViewEntity;
import graphic.MovableComponent;
import graphic.relations.RelationGrip;
import graphic.textbox.TextBox;
import graphic.textbox.TextBoxAttribute;
import graphic.textbox.TextBoxEntityName;
import graphic.textbox.TextBoxMethod;
import graphic.textbox.TextBoxMethod.ParametersViewStyle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import swing.PropertyLoader;
import swing.SPanelElement;
import swing.Slyum;
import utility.PersonalizedIcon;
import utility.Utility;
import change.BufferBounds;
import change.Change;
import classDiagram.IDiagramComponent;
import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.Attribute;
import classDiagram.components.Entity;
import classDiagram.components.Method;
import classDiagram.components.PrimitiveType;
import classDiagram.components.Visibility;

/**
 * Represent the view of an entity in UML structure.
 * 
 * @author David Miserez
 * @version 1.0 - 25.07.2011
 */
public abstract class EntityView 
    extends MovableComponent 
    implements Observer, ColoredComponent, Cloneable {
  public static final Color baseColor = new Color(255, 247, 225);
  private static Color basicColor = new Color(baseColor.getRGB());

  public static final float BORDER_WIDTH = 1.2f;
  public static final int VERTICAL_SPACEMENT = 10; // margin

    /**
     * Get the default color used then a new entity is created.
     * 
     * @return the basic color.
     */
    public static Color getBasicColor() {
        String colorEntities = PropertyLoader.getInstance().getProperties()
                .getProperty(PropertyLoader.COLOR_ENTITIES);
        Color color;

        if (colorEntities == null)
            color = basicColor;
        else
            color = new Color(Integer.parseInt(colorEntities));

        return color;
    };

    /**
     * Compute the point intersecting the lines given. Return Point(-1.0f,
     * -1.0f) if liens are //.
     * 
     * @param line1
     *            the first line
     * @param line2
     *            the second line
     * @return the intersection point of the two lines
     */
    public static Point2D ptIntersectsLines(Line2D line1, Line2D line2) {
        // convert line2D to point
        final Point p1 = new Point((int) line1.getP1().getX(), (int) line1
                .getP1().getY());
        final Point p2 = new Point((int) line1.getP2().getX(), (int) line1
                .getP2().getY());
        final Point p3 = new Point((int) line2.getP1().getX(), (int) line2
                .getP1().getY());
        final Point p4 = new Point((int) line2.getP2().getX(), (int) line2
                .getP2().getY());

        // compute intersection point between two line
        // (http://en.wikipedia.org/wiki/Line-line_intersection)
        final int denom = (p1.x - p2.x) * (p3.y - p4.y) - (p1.y - p2.y)
                * (p3.x - p4.x);

        // no intersection (lines //)
        if (denom == 0)
            return new Point2D.Float(-1.0f, -1.0f);

        final int x = ((p1.x * p2.y - p1.y * p2.x) * (p3.x - p4.x) - (p1.x - p2.x)
                * (p3.x * p4.y - p3.y * p4.x))
                / denom;
        final int y = ((p1.x * p2.y - p1.y * p2.x) * (p3.y - p4.y) - (p1.y - p2.y)
                * (p3.x * p4.y - p3.y * p4.x))
                / denom;

        return new Point2D.Float(x, y);
    }

    /**
     * Search the intersection point between the border of a rectangle and the
     * line defined by first and next point. The rectangle is decomposed in for
     * lines and each line go to infinite. So all lines intersect an edge of the
     * rectangle. We must compute if segments intersect each others or not.
     * 
     * @param bounds
     *            the rectangle
     * @param first
     *            the first point
     * @param next
     *            the next point
     * @return the intersection point; or null if no points found
     */
    public static Point searchNearestEgde(
        Rectangle bounds, Point first, Point next) {
      
        // One offset needed to avoid intersection with the wrong line.
        if (bounds.x + bounds.width <= first.x)
            first.x = bounds.x + bounds.width - 1;
        else if (bounds.x >= first.x)
            first.x = bounds.x + 1;

        if (bounds.y + bounds.height <= first.y)
            first.y = bounds.height + bounds.y - 1;
        else if (bounds.y >= first.y)
            first.y = bounds.y + 1;

        Line2D relationLine =  
            new Line2D.Float(first.x, first.y, next.x, next.y);
        Line2D lineTop = 
            new Line2D.Float(bounds.x, bounds.y, bounds.x + bounds.width,
                             bounds.y);
        Line2D lineRight = 
            new Line2D.Float(bounds.x + bounds.width,
                bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
        Line2D lineBottom = 
            new Line2D.Float(bounds.x + bounds.width, bounds.y + bounds.height,
                             bounds.x, bounds.y + bounds.height);
        Line2D lineLeft = 
            new Line2D.Float(bounds.x, bounds.y + bounds.height,
                             bounds.x, bounds.y);

        Point2D ptIntersectTop =
            ptIntersectsLines(relationLine, lineTop);
        Point2D ptIntersectRight =
            ptIntersectsLines(relationLine, lineRight);
        Point2D ptIntersectBottom =
            ptIntersectsLines(relationLine, lineBottom);
        Point2D ptIntersectLeft =
            ptIntersectsLines(relationLine, lineLeft);

        // line is to infinite, we must verify that the point find interst the
        // correct edge and the relation.
        int distTop = 
            (int) lineTop.ptSegDist(ptIntersectTop) + 
            (int) relationLine.ptSegDist(ptIntersectTop);
        int distRight =
            (int) lineRight.ptSegDist(ptIntersectRight) +
            (int) relationLine.ptSegDist(ptIntersectRight);
        int distBottom =
            (int) lineBottom.ptSegDist(ptIntersectBottom) +
            (int) relationLine.ptSegDist(ptIntersectBottom);
        int distLeft = 
            (int) lineLeft.ptSegDist(ptIntersectLeft) + 
            (int) relationLine.ptSegDist(ptIntersectLeft);

        if (ptIntersectTop != null && distTop == 0) {
            return new Point(
                RelationGrip.adjust((int) ptIntersectTop.getX()),
                (int) ptIntersectTop.getY());
        
        } else if (ptIntersectRight != null && distRight == 0) {
            return new Point(
                (int) ptIntersectRight.getX(),
                RelationGrip.adjust((int) ptIntersectRight.getY()));
        
        } else if (ptIntersectBottom != null && distBottom == 0) {
            return new Point(
                RelationGrip.adjust((int) ptIntersectBottom.getX()),
                (int) ptIntersectBottom.getY());
        
        } else if (ptIntersectLeft != null && distLeft == 0) {
            return new Point(
                (int) ptIntersectLeft.getX(),
                RelationGrip.adjust((int) ptIntersectLeft.getY()));
        
        } else {
            return null; // no point found!
        }
    }

    /**
     * Set the basic color. Basic color is used as default color while creating
     * a new entity.
     * 
     * @param color
     *            the new basic color
     */
    public static void setBasicColor(Color color) {
        basicColor = new Color(color.getRGB());
    }

    /* Colors */
    public final Color DEFAULT_TEXT_COLOR = new Color(40, 40, 40);
    public final Color DEFAULT_BORDER_COLOR = new Color(65, 65, 65);

    private Rectangle bounds = new Rectangle();
    protected Entity component;
    private Color defaultColor;

    private boolean displayDefault = true;
    private boolean displayAttributes = true;
    protected boolean displayMethods = true;

    private final TextBoxEntityName entityName;

    protected LinkedList<TextBoxAttribute> attributesView = new LinkedList<TextBoxAttribute>();
    protected LinkedList<TextBoxMethod> methodsView = new LinkedList<TextBoxMethod>();

    private TextBox pressedTextBox;
    private JMenuItem menuItemDelete, menuItemMoveUp, menuItemMoveDown,
                      menuItemStatic, menuItemAbstract, menuItemViewDefault,
                      menuItemViewAll, menuItemViewAttributes, menuItemViewMethods,
                      menuItemViewNothing, menuItemMethodsDefault,
                      menuItemMethodsAll, menuItemMethodsType, 
                      menuItemMethodsName, menuItemMethodsNothing;
    private ButtonGroup groupView, groupViewMethods;

    private Cursor saveCursor = Cursor.getDefaultCursor();

    protected GraphicComponent saveTextBoxMouseHover;

    private static final Font stereotypeFontBasic = new Font(
            Slyum.getInstance().defaultFont.getFamily(), 0, 11);
    private Font stereotypeFont = stereotypeFontBasic;

    public EntityView(final GraphicView parent, Entity component) {
        super(parent);

        if (component == null)
            throw new IllegalArgumentException("component is null");

        this.component = component;
        
        JMenu subMenu;
        JMenuItem menuItem;

        // Create a textBox for display the entity name.
        entityName = new TextBoxEntityName(parent, component);

        // Create the popup menu.
        popupMenu.addSeparator();

        popupMenu.add(
            makeMenuItem("Add attribute", "AddAttribute", "attribute"));
        popupMenu.add(makeMenuItem("Add method", "AddMethod", "method"));
        
        popupMenu.addSeparator();

        popupMenu.add(menuItemAbstract = 
            makeMenuItem("Abstract", "Abstract", "abstract"));
        popupMenu.add( menuItemStatic = 
            makeMenuItem("Static", "Static", "static"));
        
        menuItemMoveUp = 
            makeMenuItem("Move up", Slyum.ACTION_TEXTBOX_UP, "arrow-up");
        menuItemMoveUp.setEnabled(false);
        popupMenu.add(menuItemMoveUp);
        
        menuItemMoveDown =
            makeMenuItem("Move down", Slyum.ACTION_TEXTBOX_DOWN, "arrow-down");
        menuItemMoveDown.setEnabled(false);
        popupMenu.add(menuItemMoveDown);
        
        popupMenu.addSeparator();
        
        popupMenu.add(
            makeMenuItem("Duplicate", Slyum.ACTION_DUPLICATE, "duplicate"));
        popupMenu.add(
            menuItemDelete = makeMenuItem("Delete", "Delete", "delete"));
        
        popupMenu.addSeparator();

        // Menu VIEW
        subMenu = new JMenu("View");
        subMenu.setIcon(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "eye.png"));
        groupView = new ButtonGroup();
        
        // Item Default
        menuItemViewDefault = makeRadioButtonMenuItem(
            "Default", "ViewDefault", groupView);
        menuItemViewDefault.setSelected(true);
        subMenu.add(menuItemViewDefault);
        
        // Item All
        subMenu.add(menuItemViewAll = makeRadioButtonMenuItem("All", "ViewAll", groupView), 1);

        // Item Only attributes
        subMenu.add(
            menuItemViewAttributes = makeRadioButtonMenuItem(
                "Only attributes", "ViewAttribute", groupView), 2);

        // Item Only methods
        subMenu.add(
            menuItemViewMethods = makeRadioButtonMenuItem(
                "Only Methods", "ViewMethods", groupView), 3);

        // Item Nothing
        subMenu.add(menuItemViewNothing = 
            makeRadioButtonMenuItem("Nothing", "ViewNothing", groupView));

        popupMenu.add(subMenu);

        // Menu VIEW METHODS
        subMenu = new JMenu("Methods View");
        subMenu.setIcon(
            PersonalizedIcon.createImageIcon(Slyum.ICON_PATH + "eye.png"));
        groupViewMethods = new ButtonGroup();
        
        menuItemMethodsDefault = makeRadioButtonMenuItem(
            "Default", "ViewMethodsDefault", groupViewMethods);
        menuItemMethodsDefault.setSelected(true);
        subMenu.add(menuItemMethodsDefault);

        subMenu.add(menuItemMethodsAll = makeRadioButtonMenuItem(
            "Type and Name", "ViewTypeAndName", groupViewMethods), 1);

        subMenu.add(menuItemMethodsType = 
            makeRadioButtonMenuItem("Type", "ViewType", groupViewMethods), 2);

        subMenu.add(menuItemMethodsName = 
            makeRadioButtonMenuItem("Name", "ViewName", groupViewMethods), 3);

        subMenu.add(menuItemMethodsNothing = 
            makeRadioButtonMenuItem(
                "Nothing", "ViewMethodNothing", groupViewMethods));

        popupMenu.add(subMenu);

        popupMenu.addSeparator();

        SPanelElement p = SPanelElement.getInstance();
        menuItem = makeMenuItem("Move top", "ZOrderTOP", "top");
        p.getBtnTop().linkComponent(menuItem);
        popupMenu.add(menuItem);

        menuItem = makeMenuItem("Up", "ZOrderUP", "up");
        p.getBtnUp().linkComponent(menuItem);
        popupMenu.add(menuItem);

        menuItem = makeMenuItem("Down", "ZOrderDown", "down");
        p.getBtnDown().linkComponent(menuItem);
        popupMenu.add(menuItem);

        menuItem = makeMenuItem("Move bottom", "ZOrderBottom", "bottom");
        p.getBtnBottom().linkComponent(menuItem);
        popupMenu.add(menuItem);

        component.addObserver(this);
        setColor(getBasicColor());
        
        initViewType();
    }
    
    public void initViewType() {
      
      if (displayDefault) {
        ViewEntity view = GraphicView.getDefaultViewEntities();
        
        switch (view) {
        case ALL:
          displayAttributes = true;
          displayMethods = true;
          break;
        case NOTHING:
          displayAttributes = false;
          displayMethods = false;
          break;
        case ONLY_ATTRIBUTES:
          displayAttributes = true;
          displayMethods = false;
          break;
        case ONLY_METHODS:
          displayAttributes = false;
          displayMethods = true;
          break;
        default:
          displayAttributes = true;
          displayMethods = true;
          break;
        
        }
        updateHeight();
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        if ("AddMethod".equals(e.getActionCommand())) {
            addMethod();
        } else if ("AddAttribute".equals(e.getActionCommand())) {
            addAttribute();
        } else if ("Delete".equals(e.getActionCommand())) {
          if (pressedTextBox != null)
              removeTextBox(pressedTextBox);
          else {
            _delete(); 
          }
        } else if ("ViewDefault".equals(e.getActionCommand())) {
          parent.setDefaultForSelectedEntities(true);
        } else if ("ViewAttribute".equals(e.getActionCommand())) {
          parent.showAttributsForSelectedEntity(true);
          parent.showMethodsForSelectedEntity(false);
        } else if ("ViewMethods".equals(e.getActionCommand())) {
            parent.showAttributsForSelectedEntity(false);
            parent.showMethodsForSelectedEntity(true);
        } else if ("ViewAll".equals(e.getActionCommand())) {
            parent.showAttributsForSelectedEntity(true);
            parent.showMethodsForSelectedEntity(true);
        } else if ("ViewNothing".equals(e.getActionCommand())) {
            parent.showAttributsForSelectedEntity(false);
            parent.showMethodsForSelectedEntity(false);
        } else if ("ViewMethodsDefault".equals(e.getActionCommand()))
          methodViewChangeClicked(ParametersViewStyle.DEFAULT);
        else if ("ViewTypeAndName".equals(e.getActionCommand()))
            methodViewChangeClicked(ParametersViewStyle.TYPE_AND_NAME);
        else if ("ViewType".equals(e.getActionCommand()))
            methodViewChangeClicked(ParametersViewStyle.TYPE);
        else if ("ViewName".equals(e.getActionCommand()))
            methodViewChangeClicked(ParametersViewStyle.NAME);
        else if ("ViewMethodNothing".equals(e.getActionCommand()))
            methodViewChangeClicked(ParametersViewStyle.NOTHING);
        else if (Slyum.ACTION_TEXTBOX_UP.equals(e.getActionCommand())
                || Slyum.ACTION_TEXTBOX_DOWN.equals(e.getActionCommand())) {
            int offset = 1;
            if (Slyum.ACTION_TEXTBOX_UP.equals(e.getActionCommand()))
                offset = -1;
            if (pressedTextBox.getClass() == TextBoxAttribute.class) {
                final Attribute attribute = (Attribute) ((TextBoxAttribute) pressedTextBox)
                        .getAssociedComponent();
                component.moveAttributePosition(attribute, offset);
            } else if (pressedTextBox.getClass() == TextBoxMethod.class) {
                final Method method = (Method) ((TextBoxMethod) pressedTextBox)
                        .getAssociedComponent();
                component.moveMethodPosition(method, offset);
            }
            component.notifyObservers();
        } else if ("Abstract".equals(e.getActionCommand())) {
          IDiagramComponent component;
          if (pressedTextBox == null) {
            component = getAssociedComponent();
            ((Entity)component).setAbstract(!((Entity)component).isAbstract());
          } else {
            component = pressedTextBox.getAssociedComponent();
            ((Method)component).setAbstract(!((Method)component).isAbstract());
          }
          component.notifyObservers();
        } else if ("Static".equals(e.getActionCommand())) {
          IDiagramComponent component = pressedTextBox.getAssociedComponent();
          if (component instanceof Attribute)
            ((Attribute)component).setStatic(!((Attribute)component).isStatic());
          else
            ((Method)component).setStatic(!((Method)component).isStatic());
          component.notifyObservers();
        } else if (Slyum.ACTION_DUPLICATE.equals(e.getActionCommand())) {
          if (pressedTextBox == null) {
            parent.duplicateSelectedEntities();
          } else {
            IDiagramComponent component = pressedTextBox.getAssociedComponent();
            Entity entity = (Entity)getAssociedComponent();
            if (component instanceof Attribute) {
              Attribute attribute = new Attribute((Attribute)component);
              LinkedList<Attribute> attributes = entity.getAttributes();
              entity.addAttribute(attribute);
              entity.notifyObservers(UpdateMessage.ADD_ATTRIBUTE_NO_EDIT);
              entity.moveAttributePosition(
                  attribute,
                  attributes.indexOf(component) - attributes.size() + 1);
              entity.notifyObservers();
            } else {
              Method method = new Method((Method)component);
              LinkedList<Method> methods = entity.getMethods();
              entity.addMethod(method);
              entity.notifyObservers(UpdateMessage.ADD_METHOD_NO_EDIT);
              entity.moveMethodPosition(
                  method,
                  methods.indexOf(component) - methods.size() + 1);
              entity.notifyObservers();
            }
          }
        }
    }

    /**
     * Create a new attribute with default type and name.
     */
    public void addAttribute() {
        final Attribute attribute = new Attribute("attribute",
                PrimitiveType.VOID_TYPE);
        prepareNewAttribute(attribute);

        component.addAttribute(attribute);
        component.notifyObservers(UpdateMessage.ADD_ATTRIBUTE);
    }

    /**
     * Create a new attribute view with the given attribute. If editing is a
     * true, the new attribute view will be in editing mode while it created.
     * 
     * @param attribute
     *            the attribute UML
     * @param editing
     *            true if creating a new attribute view in editing mode; false
     *            otherwise
     */
    public void addAttribute(Attribute attribute, boolean editing) {
        final TextBoxAttribute newTextBox = new TextBoxAttribute(parent,
                attribute);
        attributesView.add(newTextBox);

        updateHeight();

        if (editing)
            newTextBox.editing();
    }

    /**
     * Create a new method with default type and name, without parameter.
     */
    public void addMethod() {
        final Method method = new Method("method", PrimitiveType.VOID_TYPE,
                Visibility.PUBLIC, component);
        prepareNewMethod(method);

        if (component.addMethod(method))
            component.notifyObservers(UpdateMessage.ADD_METHOD);
    }

    /**
     * Create a new method view with the given method. If editing is a true, the
     * new method view will be in editing mode while it created.
     * 
     * @param method
     *            the method UML
     * @param editing
     *            true if creating a new method view in editing mode; false
     *            otherwise
     */
    public void addMethod(Method method, boolean editing) {
        final TextBoxMethod newTextBox = new TextBoxMethod(parent, method);
        methodsView.add(newTextBox);

        updateHeight();

        if (editing)
            newTextBox.editing();
    }

    /**
     * Adjust the width according to its content.
     */
    public void adjustWidth() {
        int width = Short.MIN_VALUE;

        for (final TextBox tb : getAllTextBox()) {
            final int tbWidth = tb.getTextDim().width;

            if (tbWidth > width)
                width = tbWidth; // get the longer content
        }

        // change the width according to the grid
        final Rectangle bounds = getBounds();

        Change.push(new BufferBounds(this));
        setBounds(new Rectangle(bounds.x, bounds.y, width
                + GraphicView.getGridSize() + 15, bounds.height));
        Change.push(new BufferBounds(this));
    }

    @Override
    public Point computeAnchorLocation(Point first, Point next) {
        return searchNearestEgde(getBounds(), first, next);
    }

    /**
     * Compute the height of the class with margin and content.
     * 
     * @param classNameHeight
     *            the height of class name
     * @param stereotypeHeight
     *            the height of stereotype
     * @param elementsHeight
     *            the height of each element (methods, attributes)
     * @return the height of the class
     */
    public int computeHeight(int classNameHeight, int stereotypeHeight,
            int elementsHeight) {
        int height = VERTICAL_SPACEMENT;

        if (!component.getStereotype().isEmpty())
            height += stereotypeHeight;

        height += classNameHeight;

        if (displayMethods)
            height += 10 + elementsHeight * methodsView.size();

        if (displayAttributes)
            height += 10 + elementsHeight * attributesView.size();

        return height + 10;
    }
    
    public void _delete() {

      boolean isRecord = Change.isRecord();
      Change.record();
      
      delete();

      if (!isRecord)
        Change.stopRecord();
    }

    @Override
    public void delete() {
        super.delete();

        parent.removeComponent(leftMovableSquare);
        parent.removeComponent(rightMovableSquare);
    }

    @Override
    public void drawSelectedEffect(Graphics2D g2) {
      if (pictureMode)
        return;
      
      final Color backColor = getColor();
      final Color fill = new Color(backColor.getRed(), backColor.getGreen(),
              backColor.getBlue(), 100);
      
      final Color border = backColor.darker();
      final BasicStroke borderStroke = new BasicStroke(1.0f,
              BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
              new float[] { 2.0f }, 0.0f);
      
      g2.setColor(fill);
      g2.fillRect(ghost.x, ghost.y, ghost.width, ghost.height);
      
      g2.setColor(border);
      g2.setStroke(borderStroke);
      g2.drawRect(ghost.x, ghost.y, ghost.width - 1, ghost.height - 1);
    }

    /**
     * Draw a border representing a selection.
     * 
     * @param g2
     *            the graphic context
     */
    public void drawSelectedStyle(Graphics2D g2) {
        final int PADDING = 2;
        final Color selectColor = new Color(100, 100, 100);

        final BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, new float[] { 2f }, 0.0f);

        final Rectangle inRectangle = new Rectangle(bounds.x + PADDING,
                bounds.y + PADDING, bounds.width - 2 * PADDING, bounds.height
                        - 2 * PADDING);

        final Rectangle outRectangle = new Rectangle(bounds.x - PADDING,
                bounds.y - PADDING, bounds.width + 2 * PADDING, bounds.height
                        + 2 * PADDING);

        g2.setStroke(dashed);
        g2.setColor(selectColor);

        g2.drawRect(inRectangle.x, inRectangle.y, inRectangle.width,
                inRectangle.height);
        g2.drawRect(outRectangle.x, outRectangle.y, outRectangle.width,
                outRectangle.height);
    }

    /**
     * get all textBox displayed by the entity. TextBox returned are: - textBox
     * for entity name - textBox for attributes - textBox for methods
     * 
     * @return an array containing all TextBox
     */
    public LinkedList<TextBox> getAllTextBox() {
        final LinkedList<TextBox> tb = new LinkedList<TextBox>();

        tb.add(entityName);
        tb.addAll(methodsView);
        tb.addAll(attributesView);

        return tb;
    }

    @Override
    public IDiagramComponent getAssociedComponent() {
        return component;
    }

    @Override
    public Rectangle getBounds() {
        if (bounds == null)
            bounds = new Rectangle();

        return new Rectangle(bounds);
    }

    /**
     * Get the entity (UML) associed with this entity view. Same as
     * getAssociedComponent().
     * 
     * @return the component associed.
     */
    public Entity getComponent() {
        return component;
    }

    public Color getDefaultColor() {
        return getBasicColor();
    }

    @Override
    public void gMouseClicked(MouseEvent e) {
        super.gMouseClicked(e);

        final TextBox textBox = GraphicView.searchComponentWithPosition(
                getAllTextBox(), e.getPoint());

        if (textBox != null) {
            final IDiagramComponent idc = textBox.getAssociedComponent();

            if (idc != null) {
                idc.select();
                idc.notifyObservers(UpdateMessage.SELECT);
            }

            if (e.getClickCount() == 2)

                textBox.editing();
        }
    }

    @Override
    public void gMouseEntered(MouseEvent e) {
        super.gMouseEntered(e);

        setMouseHoverStyle();

        saveCursor = parent.getScene().getCursor();
        parent.getScene().setCursor(new Cursor(Cursor.MOVE_CURSOR));
    }

    @Override
    public void gMouseExited(MouseEvent e) {
        super.gMouseExited(e);

        if (saveTextBoxMouseHover != null) {
            saveTextBoxMouseHover.gMouseExited(e);
            saveTextBoxMouseHover = null;
        }

        setDefaultStyle();

        parent.getScene().setCursor(saveCursor);
    }

    @Override
    public void gMouseMoved(MouseEvent e) {
        final GraphicComponent textBoxMouseHover = GraphicView
                .searchComponentWithPosition(getAllTextBox(), e.getPoint());
        GraphicView.computeComponentEventEnter(textBoxMouseHover,
                saveTextBoxMouseHover, e);

        saveTextBoxMouseHover = textBoxMouseHover;
    }

    @Override
    public void gMousePressed(MouseEvent e) {
        pressedTextBox = searchTextBoxAtLocation(e.getPoint());
        super.gMousePressed(e);
    }

    /**
     * Search and return the Textbox (methods and attributes) at the given
     * location.
     * 
     * @param location
     *            the location where find a TextBox
     * @return the found TextBox
     */
    private TextBox searchTextBoxAtLocation(Point location) {
        final LinkedList<TextBox> tb = getAllTextBox();
        tb.remove(entityName);
        return GraphicView.searchComponentWithPosition(tb, location);
    }

    @Override
    public boolean isAtPosition(Point mouse) {
        return bounds.contains(mouse);
    }

    /**
     * Return if attributes are displayed or not.
     * 
     * @return true if attributes are displayed; false otherwise
     */
    public boolean isAttributeDisplayed() {
        return displayAttributes;
    }

    /**
     * Return if methods are displayed or not.
     * 
     * @return true if methods are displayed; false otherwise
     */
    public boolean isMethodsDisplayed() {
        return displayMethods;
    }

    @Override
    public void maybeShowPopup(MouseEvent e, JPopupMenu popupMenu) {
        if (e.isPopupTrigger()) {
            updateMenuItemView();
            updateMenuItemMethodsView();
            
            String text = "Delete ";
            menuItemAbstract.setEnabled(false);

            // If context menu is requested on a TextBox, customize popup menu.
            if (pressedTextBox != null) {
              menuItemStatic.setEnabled(true);
              
              text += pressedTextBox.getText();
              menuItemMoveUp.setEnabled(attributesView
                      .indexOf(pressedTextBox) != 0
                      && methodsView.indexOf(pressedTextBox) != 0);
              menuItemMoveDown
                      .setEnabled((attributesView.size() == 0 || attributesView
                              .indexOf(pressedTextBox) != attributesView
                              .size() - 1)
                              && (methodsView.size() == 0 || methodsView
                                      .indexOf(pressedTextBox) != methodsView
                        .size() - 1));
              if (pressedTextBox instanceof TextBoxMethod)
                menuItemAbstract.setEnabled(true);
                
            } else {
                text += component.getName();
                menuItemMoveUp.setEnabled(false);
                menuItemMoveDown.setEnabled(false);
                menuItemStatic.setEnabled(false);
                menuItemAbstract.setEnabled(true);
            }
            menuItemDelete.setText(text);
        }

        super.maybeShowPopup(e, popupMenu);
    }

    /**
     * Change the display style of parameters for all methods.
     * 
     * @param newStyle
     *            the new display style
     */
    public void methodViewChange(ParametersViewStyle newStyle) {
      for (TextBoxMethod tbm : methodsView)
        tbm.setParametersViewStyle(newStyle);
    }

    /**
     * Change the display style of parameters for the pressed TextBox if exists,
     * or for all otherwise.
     * 
     * @param newStyle
     *            the new display style
     */
    private void methodViewChangeClicked(ParametersViewStyle newStyle) {
      if (pressedTextBox instanceof TextBoxMethod)
        ((TextBoxMethod) pressedTextBox).setParametersViewStyle(newStyle);
      else
        for (EntityView ev : parent.getSelectedEntities())
          ev.methodViewChange(newStyle);
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        if (!isVisible())
            return;

        Paint background;
        if (GraphicView.isEntityGradient())
          background = new GradientPaint(bounds.x, bounds.y, getColor(),
              bounds.x + bounds.width, bounds.y + bounds.height,
              getColor().darker());
        else
          background = getColor();
        
        String className = component.getName();

        FontMetrics classNameMetrics = g2.getFontMetrics(entityName
                .getEffectivFont());
        int classNameWidth = classNameMetrics.stringWidth(className);
        int classNameHeight = classNameMetrics.getHeight();

        Dimension classNameSize = new Dimension(classNameWidth, classNameHeight);

        stereotypeFont = stereotypeFont.deriveFont(stereotypeFontBasic
                .getSize() * parent.getZoom());

        g2.setFont(stereotypeFont);
        String stereotype = Utility.truncate(g2,
                "<<" + component.getStereotype() + " >>", bounds.width - 15);

        FontMetrics stereotypeMetrics = g2.getFontMetrics(stereotypeFont);
        int stereotypeWidth = stereotypeMetrics.stringWidth(stereotype);
        int stereotypeHeight = stereotypeMetrics.getHeight();

        Dimension stereotypeSize = new Dimension(stereotypeWidth,
                stereotypeHeight);

        FontMetrics metrics = g2.getFontMetrics(entityName.getEffectivFont());
        int textBoxHeight = metrics.getHeight();

        bounds.height = computeHeight(classNameSize.height, stereotypeHeight,
                textBoxHeight);

        Rectangle bounds = getBounds();

        int offset = bounds.y + VERTICAL_SPACEMENT / 2;
        int stereotypeLocationWidth = bounds.x
                + (bounds.width - stereotypeSize.width) / 2;

        entityName.setBounds(new Rectangle(1, 1, bounds.width - 15,
                textBoxHeight + 2));
        Rectangle entityNameBounds = entityName.getBounds();
        int classNameLocationX = bounds.x
                + (bounds.width - entityNameBounds.width) / 2;

        // draw background
        g2.setPaint(background);
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        // draw border
        g2.setStroke(new BasicStroke(BORDER_WIDTH));
        g2.setColor(DEFAULT_BORDER_COLOR);
        g2.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        // draw stereotype
        if (!component.getStereotype().isEmpty()) {
            offset += stereotypeSize.height;

            g2.setFont(stereotypeFont);
            g2.setColor(DEFAULT_TEXT_COLOR);
            g2.drawString(stereotype, stereotypeLocationWidth, offset);
        }

        // draw class name
        offset += /* classNameSize.height + */VERTICAL_SPACEMENT / 2;

        entityName.setBounds(new Rectangle(classNameLocationX, offset,
                bounds.width - 15, textBoxHeight + 2));
        entityName.paintComponent(g2);

        offset += entityNameBounds.height;

        if (displayAttributes) {
          // draw attributs separator
          offset += 10;
          g2.setStroke(new BasicStroke(BORDER_WIDTH));
          g2.setColor(DEFAULT_BORDER_COLOR);
          g2.drawLine(bounds.x, offset, bounds.x + bounds.width, offset);
  
          // draw attributes
          for (TextBoxAttribute tb : attributesView) {
              tb.setBounds(new Rectangle(bounds.x + 8, offset + 2,
                      bounds.width - 15, textBoxHeight + 2));
              tb.paintComponent(g2);

              offset += textBoxHeight;
          }
        }

        if (displayMethods) {
          // draw methods separator
          offset += 10;
          g2.setStroke(new BasicStroke(BORDER_WIDTH));
          g2.setColor(DEFAULT_BORDER_COLOR);
          g2.drawLine(bounds.x, offset, bounds.x + bounds.width, offset);
  
          // draw methods
          for (final TextBoxMethod tb : methodsView) {
            tb.setBounds(new Rectangle(bounds.x + 8, offset + 2,
                bounds.width - 15, textBoxHeight + 2));
            tb.paintComponent(g2);
            offset += textBoxHeight;
          }
        }

        // is component selected? -> draw selected style
        if (!pictureMode && parent.getSelectedComponents().contains(this))
            drawSelectedStyle(g2);
    }

    /**
     * Method called before creating a new attribute, if modifications on
     * attribute is necessary.
     * 
     * @param attribute
     *            the attribute to prepare
     */
    protected abstract void prepareNewAttribute(Attribute attribute);

    /**
     * Method called before creating a new method, if modifications on method is
     * necessary.
     * 
     * @param method
     *            the method to prepare
     */
    protected abstract void prepareNewMethod(Method method);

    /**
     * Delete all TextBox and regenerate them. !! This method take time !!
     */
    public void regenerateEntity() {
        boolean isStopRepaint = parent.getStopRepaint();
        parent.setStopRepaint(true);

        methodsView.clear();
        attributesView.clear();

        entityName.setText(component.getName());

        for (final Attribute a : component.getAttributes())
            addAttribute(a, false);

        for (final Method m : component.getMethods())
            addMethod(m, false);

        if (!isStopRepaint)
            parent.goRepaint();

        updateHeight();
    }

    /**
     * Remove the attribute associated with TextBoxAttribute from model (UML).
     * 
     * @param tbAttribute
     *            the attribute to remove.
     * @return true if the attribute has been removed; false otherwise
     */
    public boolean removeAttribute(TextBoxAttribute tbAttribute) {
        if (component.removeAttribute((Attribute) tbAttribute
                .getAssociedComponent())) {
            component.notifyObservers();

            updateHeight();

            return true;
        }

        return false;
    }

    /**
     * Remove the method associated with TextBoxMethod from model (UML)
     * 
     * @param tbMethod
     *            the method to remove.
     * @return true if component has been removed; false otherwise.
     */
    public boolean removeMethod(TextBoxMethod tbMethod) {
        if (component.removeMethod((Method) tbMethod.getAssociedComponent())) {
            component.notifyObservers();

            updateHeight();

            return true;
        }

        return false;
    }

    /**
     * Generic method for remove the associated component for the given TextBox.
     * 
     * @param tb
     *            the TextBox containing the element to remove.
     * @return true if component has been removed; false otherwise.
     */
    public boolean removeTextBox(TextBox tb) {
        // Need to find a best way
        if (tb instanceof TextBoxAttribute)

            return removeAttribute((TextBoxAttribute) tb);

        else if (tb instanceof TextBoxMethod)

            return removeMethod((TextBoxMethod) tb);

        return false;
    }

    @Override
    public void repaint() {
        parent.getScene().repaint(getBounds());
    }

    @Override
    public void setBounds(Rectangle bounds) {
        // Save current bounds, change bounds and repaint old bounds and new
        // bounds.
        final Rectangle repaintBounds = new Rectangle(getBounds());

        final Rectangle newBounds = new Rectangle(ajustOnGrid(bounds.x),
                ajustOnGrid(bounds.y), ajustOnGrid(bounds.width), bounds.height);

        newBounds.width = newBounds.width < MINIMUM_SIZE.x ? MINIMUM_SIZE.x
                : newBounds.width;

        this.bounds = newBounds;

        parent.getScene().repaint(repaintBounds);
        parent.getScene().repaint(newBounds);

        // Move graphics elements associated with this component
        leftMovableSquare.setBounds(computeLocationResizer(0));
        rightMovableSquare.setBounds(computeLocationResizer(bounds.width));

        setChanged();
        notifyObservers();
    }

    @Override
    public void setColor(Color color) {
        setCurrentColor(color);
        defaultColor = color;
    }

    /**
     * Set the current color for this entity.
     * 
     * @param color
     *            the current color.
     */
    public void setCurrentColor(Color color) {
        super.setColor(color);
    }

    @Override
    public void setDefaultStyle() {
        setCurrentColor(defaultColor);
        repaint();
    }
    
    @Override
    public Color getColor() {
      if (pictureMode)
        return defaultColor;
      return super.getColor();
    }
    
    public void setDisplayDefault(boolean display) {
      displayDefault = display;
      initViewType();
    }

    /**
     * Set the display state for attributes.
     * @param display the new display state for attributes.
     */
    public void setDisplayAttributes(boolean display) {
      displayAttributes = display;
      displayDefault = false;
      updateHeight();
    }

    /**
     * Set the display state for methods.
     * @param display the new display state for methods.
     */
    public void setDisplayMethods(boolean display) {
      displayMethods = display;
      displayDefault = false;
      updateHeight();
    }
    
    private void updateMenuItemView() {
      JMenuItem menuItemToSelect;
      
      // Check si toutes les entit�s s�lectionn�es ont le m�me type de vue.
      List<EntityView> selected = parent.getSelectedEntities();
      for (int i = 0; i < selected.size() - 1; i++) {
        EntityView view = selected.get(i),
                   next = selected.get(i+1);
        if (view.displayAttributes != next.displayAttributes ||
            view.displayMethods != next.displayMethods) {
          groupView.clearSelection();
          return;
        }
      }
      
      if (displayDefault)
        menuItemToSelect = menuItemViewDefault;
      else if (displayAttributes && displayMethods)
        menuItemToSelect = menuItemViewAll;
      else if (displayAttributes)
        menuItemToSelect = menuItemViewAttributes;
      else if (displayMethods)
        menuItemToSelect = menuItemViewMethods;
      else
        menuItemToSelect = menuItemViewNothing;
      
      groupView.setSelected(menuItemToSelect.getModel(), true);
    }
    
    private void updateMenuItemMethodsView() {
      JMenuItem itemToSelect;
      ParametersViewStyle newView = null;

      if (pressedTextBox == null) {
        // Check si toutes les m�thodes des entit�s s�lectionn�es ont la m�me vue.
        List<EntityView> selected = parent.getSelectedEntities();
        List<TextBoxMethod> textbox = new LinkedList<>();
        for (EntityView view : selected)
          textbox.addAll(view.methodsView);
        
        for (int i = 0; i < textbox.size() - 1; i++) {
          TextBoxMethod current = textbox.get(i),
                        next = textbox.get(i+1);
          if (!current.getConcretParametersViewStyle().equals(
               next.getConcretParametersViewStyle())) {
            groupViewMethods.clearSelection();
            return;
          }
        }
        
        if (textbox.size() > 0)
          newView = textbox.get(0).getConcretParametersViewStyle();
      } else if (pressedTextBox instanceof TextBoxMethod) {
        newView = 
            ((TextBoxMethod)pressedTextBox).getConcretParametersViewStyle();
      }
      
      if (newView != null) {
        switch (newView) {
          case DEFAULT:
            itemToSelect = menuItemMethodsDefault;
            break;
          case NAME:
            itemToSelect = menuItemMethodsName;
            break;
            
          case NOTHING:
            itemToSelect = menuItemMethodsNothing;
            break;
            
          case TYPE:
            itemToSelect = menuItemMethodsType;
            break;
            
          case TYPE_AND_NAME:
            itemToSelect = menuItemMethodsAll;
            break;
  
          default:
            itemToSelect = menuItemMethodsAll;
            break;
        }
        
        groupViewMethods.setSelected(itemToSelect.getModel(), true);
      }
    }

    @Override
    public void setMouseHoverStyle() {
      setCurrentColor(getColor().brighter());
      repaint();
    }

    @Override
    public void setSelected(boolean select) {
      super.setSelected(select);
      component.select();

      if (select)
        component.notifyObservers(UpdateMessage.SELECT);
      else
        component.notifyObservers(UpdateMessage.UNSELECT);

      if (!select)
        for (final TextBox t : getAllTextBox())
            t.setSelected(false);
    }

    @Override
    public void setStyleClicked() {
      setCurrentColor(getColor().darker());
      repaint();
    }

    @Override
    public String toXML(int depth) {
        final String tab = Utility.generateTab(depth);

        String xml = tab + 
            "<componentView componentID=\"" + getAssociedComponent().getId() + "\" " +
            		           "color=\"" + defaultColor.getRGB() + "\" " +
            		           "displayDefault=\"" + displayDefault + "\" " +
            		           "displayAttributes=\"" + displayAttributes + "\" " +
            		           "displayMethods=\"" + displayMethods + "\" >\n";
        xml += Utility.boundsToXML(depth + 1, getBounds(), "geometry");
        xml += tab + "</componentView>\n";

        return xml;
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        boolean enable = false;
        if (arg1 != null && arg1.getClass() == UpdateMessage.class)
            switch ((UpdateMessage) arg1) {
            case SELECT:
                super.setSelected(true);
                break;

            case UNSELECT:
                super.setSelected(false);
                break;

            case ADD_ATTRIBUTE:
                enable = true;
            case ADD_ATTRIBUTE_NO_EDIT:
                addAttribute(component.getAttributes().getLast(), enable);
                break;

            case ADD_METHOD:
                enable = true;
            case ADD_METHOD_NO_EDIT:
                addMethod(component.getMethods().getLast(), enable);
                break;
            case MODIF:
              break;
            default:
              break;
            }
        else
            regenerateEntity();
    }

    /**
     * Udpate the height of the entity and notify all components.
     */
    public void updateHeight() {
      Rectangle repaintBounds = getBounds();
      parent.getScene().paintImmediately(repaintBounds);

      // set new height compute while repainting.
      setBounds(new Rectangle(bounds));
      
      parent.getScene().repaint(repaintBounds);
      setChanged();
      notifyObservers();
    }

    @Override
    public void restore() {
      super.restore();

      parent.addOthersComponents(leftMovableSquare);
      parent.addOthersComponents(rightMovableSquare);
    }
    
    @Override
    public void setPictureMode(boolean enable) {
      super.setPictureMode(enable);
      for (TextBox t : methodsView)
        t.setPictureMode(enable);
      for (TextBox t : attributesView)
        t.setPictureMode(enable);
    }
    
    @Override
    public EntityView clone() throws CloneNotSupportedException {
      try {
        Rectangle newBounds = getBounds();
        String classToInstanciate = 
            getClass().equals(AssociationClassView.class) ? 
                ClassView.class.getName() : getClass().getName();
        int gridSize = GraphicView.getGridSize();
        newBounds.translate(gridSize, gridSize);
        Entity entity = ((Entity)getAssociedComponent()).clone();
        EntityView view = (EntityView)Class.forName(classToInstanciate)
            .getConstructor(GraphicView.class, entity.getClass())
            .newInstance(parent, entity);
        view.setBounds(newBounds);
        view.setColor(defaultColor);
        return view;
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }
    
    public TextBox searchAssociedTextBox(IDiagramComponent search) {
      for (TextBox textbox : getAllTextBox())
        if (textbox.getAssociedComponent() == search)
          return textbox;
      
      return null;
    }
}
