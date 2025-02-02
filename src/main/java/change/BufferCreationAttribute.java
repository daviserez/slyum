package change;

import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.Attribute;
import classDiagram.components.SimpleEntity;

public class BufferCreationAttribute implements Changeable {
  private SimpleEntity entity;
  private Attribute attribute;
  private boolean isCreated;
  private int index;

  public BufferCreationAttribute(SimpleEntity e, Attribute a,
                                 Boolean isCreated, int index) {
    entity = e;
    attribute = a;
    this.isCreated = isCreated;
    this.index = index;
  }

  @Override
  public void restore() {

    if (!isCreated) {
      entity.addAttribute(attribute);
      entity.notifyObservers(UpdateMessage.ADD_ATTRIBUTE_NO_EDIT);

      entity.moveAttributePosition(attribute, index
                                              - entity.getAttributes().size() + 1);

      entity.notifyObservers();
    } else {
      entity.removeAttribute(attribute);
      entity.notifyObservers();
    }

  }

  @Override
  public Object getAssociedComponent() {
    return attribute;
  }

}
