package swing.hierarchicalView;

import classDiagram.IDiagramComponent;
import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.relationships.Association;
import classDiagram.relationships.Role;
import swing.PanelClassDiagram;
import swing.hierarchicalView.HierarchicalView.STree;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.LinkedList;
import java.util.Observable;

/**
 * A JTree node associated with an association UML.
 *
 * @author David Miserez
 * @version 1.0 - 28.07.2011
 */
public class NodeAssociation extends AbstractNode {

  /**
   * Return the title that the node must show according to its association.
   *
   * @param association the association to get the title
   *
   * @return the title generated from association
   */
  public static String generateName(Association association) {
    String label = association.getName();
    if (!label.isEmpty()) return label;

    final LinkedList<Role> roles = association.getRoles();
    String text = "";
    String PREFIX = " - ";

    if (roles.isEmpty()) return "";

    for (Role role : roles)
      text += " - " + role.getEntity().getName();

    // On efface le premier préfixe.
    return text.substring(PREFIX.length());
  }

  private final Association association;
  private final ImageIcon imageIcon;

  /**
   * Create a new node association with an association.
   *
   * @param association the associated association
   * @param treeModel the model of the JTree
   * @param icon the customized icon
   * @param tree the JTree
   */
  public NodeAssociation(
      Association association, DefaultTreeModel treeModel, ImageIcon icon, STree tree) {
    super(generateName(association), treeModel, tree);

    if (treeModel == null)
      throw new IllegalArgumentException("treeModel is null");

    if (tree == null) throw new IllegalArgumentException("tree is null");

    this.tree = tree;
    this.association = association;
    association.addObserver(this);

    for (final Role role : association.getRoles())
      role.addObserver(this);

    this.treeModel = treeModel;
    imageIcon = icon;
  }

  @Override
  public IDiagramComponent getAssociedComponent() {
    return association;
  }

  @Override
  public ImageIcon getCustomizedIcon() {
    return imageIcon;
  }

  @Override
  public void remove() {
    association.getRoles().stream().forEach(role -> role.deleteObserver(this));
  }

  @Override
  public void update(Observable observable, Object o) {
    if (o != null && o instanceof UpdateMessage) {
      final TreePath path = new TreePath(getPath());

      switch ((UpdateMessage) o) {
        case SELECT:
          if (!PanelClassDiagram.getInstance().isDisabledUpdate())
            tree.addSelectionPathNoFire(path);
          break;
        case UNSELECT:
          tree.removeSelectionPathNoFire(path);
          break;
        default:
          break;
      }
    } else {
      setUserObject(generateName(association));
      treeModel.reload(this);
    }
  }

}
