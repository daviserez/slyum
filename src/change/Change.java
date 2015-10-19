package change;

import java.util.LinkedList;
import swing.PanelClassDiagram;

import swing.Slyum;

public class Change {
  private static boolean _hasChange = false;
  private static boolean addSinceLastRecord = false;
  private static boolean block = false;
  private static boolean isRecord = false;
  private static int pointer = 0;

  private static LinkedList<Boolean> record = new LinkedList<>();

  private static LinkedList<Changeable> stack = new LinkedList<>();


  public static void clear() {
    stack.clear();
    record.clear();
    pointer = 0;
    setHasChange(false);
    
    printStackState();
  }

  public static void setHasChange(boolean changed) {
    _hasChange = changed;
    
    Slyum.setStarOnTitle(changed);
    
    checkToolbarButtonState();
  }

  public static Changeable getLast() {
    return stack.getLast();
  }

  public static int getSize() {
    return stack.size();
  }

  public static boolean hasChange() {
    return _hasChange;
  }

  public static boolean isBlocked() {
    return block;
  }

  public static void setBlocked(boolean blocked) {
    block = blocked;
  }

  public static boolean isRecord() {
    return isRecord;
  }

  public static void pop() {
    if (pointer == stack.size() - 1) pointer--;
    
    stack.removeLast();
    record.removeLast();
  }

  public static void push(Changeable ch) {
    if (block) return;

    // Remove all elements positioned after index pointer.
    while (stack.size() > 1 && pointer < stack.size() - 1) {
      stack.removeLast();
      stack.removeLast();

      record.removeLast();
      record.removeLast();
    }

    stack.add(ch);
    record.add(isRecord);

    if (isRecord()) addSinceLastRecord = true;

    pointer = stack.size() - 1;

    printStackState();

    checkToolbarButtonState();

    setHasChange(true);
  }

  /**
   * Begin a record. A record merge all new pushes in a same group. When undo /
   * redo is called, all pushes into a group will be undo / redo at the same
   * time.
   */
  public static void record() {
    addSinceLastRecord = false;
    isRecord = true;
  }
  
  public static void redo() {
    if (pointer >= stack.size() - 1) return;

    final int increment = pointer % 2 == 0 ? 1 : 2;

    final boolean isBlocked = Change.isBlocked();
    setBlocked(true);
    stack.get(pointer += increment).restore();
    setBlocked(isBlocked);

    printStackState();

    checkToolbarButtonState();

    setHasChange(true);

    if (record.get(pointer)) redo();
  }

  /**
   * Stop the current record. If no record is currently running this method have
   * no effect.
   */
  public static void stopRecord() {
    int size = stack.size();

    boolean b1 = addSinceLastRecord, b2 = isRecord;

    addSinceLastRecord = false;
    isRecord = false;

    if (b2 == false || size < 1 || !b1)
      
      return;
    
    int b = pointer - 2;
    while (b >= 0 && b < size - 1 && record.get(b))
      b--;

    record.set(b + 1, false);
    record.set(pointer, false);

    printStackState();
  }
  
  public static void undo() {
    if (pointer <= 0) return;
    
    final int decrement = pointer % 2 > 0 ? 1 : 2;
    
    final boolean isBlocked = Change.isBlocked();
    setBlocked(true);
    stack.get(pointer -= decrement).restore();
    setBlocked(isBlocked);
    
    printStackState();
    
    checkToolbarButtonState();

    setHasChange(true);
    
    if (record.get(pointer))
      
      undo();
  }

  protected static void checkToolbarButtonState() {
    if (PanelClassDiagram.getInstance() == null)
      return;
    
    Slyum.setEnableRedoButtons(pointer < stack.size() - 1);
    Slyum.setEnableUndoButtons(pointer > 0);
  }
  
  private static void printStackState() {
    if (!Slyum.argumentIsChangeStackStatePrinted()) return;
    
    System.out.println("Etat de la pile");
    
    for (int i = 0; i < stack.size(); i++)
      
      System.out.println(i + " - " + record.get(i)
              + (pointer == i ? " <--" : ""));

    System.out.println("--------------");
  }

}
