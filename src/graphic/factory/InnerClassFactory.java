package graphic.factory;

import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.relations.InnerClassView;

import java.awt.Color;
import java.awt.Graphics2D;

import swing.SPanelDiagramComponent;
import utility.SMessageDialog;
import classDiagram.relationships.InnerClass;
import graphic.entity.ClassView;
import graphic.entity.EntityView;
import graphic.entity.EnumView;

public class InnerClassFactory extends RelationFactory {
  public final String ERROR_CREATION_MESSAGE = "Inner class creation failed.\nYou must make a bond between two classes or class -> interface.";

  public InnerClassFactory(GraphicView parent) {
    super(parent);

    GraphicView.setButtonFactory(SPanelDiagramComponent.getInstance()
            .getBtnInnerClass());
  }

  @Override
  public GraphicComponent create() {
    try {
      if (componentMousePressed instanceof EntityView
              && componentMouseReleased instanceof EntityView) {
        EntityView source = (EntityView) componentMousePressed;
        EntityView target = (EntityView) componentMouseReleased;

        InnerClass innerClass = new InnerClass(source.getComponent(),
                target.getComponent());
        InnerClassView i = new InnerClassView(parent, source, target,
                innerClass, mousePressed, mouseReleased, true);

        parent.addLineView(i);
        classDiagram.addInnerClass(innerClass);

        parent.unselectAll();
        i.setSelected(true);

        return i;
      }
    } catch (final IllegalArgumentException e) {
      System.err
              .println("Inheritance relation between class (child) and interface (parent) is not possible.");
    }

    repaint();
    return null;
  }

  @Override
  protected void drawExtremity(Graphics2D g2) {
    InnerClassView.paintExtremity(g2, points.get(points.size() - 1),
            mouseLocation, Color.DARK_GRAY);
  }

  @Override
  protected void creationFailed() {
    SMessageDialog.showErrorMessage(ERROR_CREATION_MESSAGE);
  }

  @Override
  protected boolean isFirstComponentValid() {
    return componentMousePressed instanceof ClassView ||
           componentMousePressed instanceof EnumView;
  }
}
