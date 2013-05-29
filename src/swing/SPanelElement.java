package swing;

import graphic.GraphicComponent;
import graphic.GraphicView;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import utility.PersonalizedIcon;
import utility.SSlider;
import utility.Utility;
import change.Change;

public class SPanelElement extends SToolBar 
                            implements ActionListener,
                                       IListenerComponentSelectionChanged {	
	private static final String TT_UNDO = 
	    "Undo " + Utility.keystrokeToString(Slyum.KEY_UNDO);
	
	private static final String TT_REDO = 
	    "Redo " + Utility.keystrokeToString(Slyum.KEY_REDO);
	
  private static final String TT_ADD_NOTE = "Link a note";
  private static final String TT_CHANGE_COLOR = "Color";
  private static final String TT_DELETE = "Delete";
  
  private static final String TT_ALIGN_TOP = 
      "Align top " + Utility.keystrokeToString(Slyum.KEY_ALIGN_UP);
  
  private static final String TT_ALIGN_BOTTOM = 
      "Align bottom " + Utility.keystrokeToString(Slyum.KEY_ALIGN_DOWN);
  
  private static final String TT_ALIGN_RIGTH = 
      "Align right " + Utility.keystrokeToString(Slyum.KEY_ALIGN_RIGHT);
  
  private static final String TT_ALIGN_LEFT = 
      "Align left " + Utility.keystrokeToString(Slyum.KEY_ALIGN_LEFT);
  
  private static final String TT_ADJUST_WIDTH = 
      "Adjust size " + Utility.keystrokeToString(Slyum.KEY_ADJUST_SIZE);

  private static final String TT_MOVE_TOP = 
      "Top" + Utility.keystrokeToString(Slyum.KEY_MOVE_TOP);
  
  private static final String TT_MOVE_UP = 
      "Up" + Utility.keystrokeToString(Slyum.KEY_MOVE_UP);
  
  private static final String TT_MOVE_DOWN = 
      "Down" + Utility.keystrokeToString(Slyum.KEY_MOVE_DOWN);
  
  private static final String TT_MOVE_BOTTOM = 
      "Bottom" + Utility.keystrokeToString(Slyum.KEY_MOVE_BOTTOM);

	private SButton undo,
	                redo,
	                btnDelete,
	                alignTop, // Alignments top.
	                alignBottom, 
	                alignRight,
	                alignLeft, 
	                adujst, // Adjust size of entity.
	                top, // z-orders
	                up, 
	                down,
	                bottom;
	
	private SSlider sliderZoom;

  private static SPanelElement instance;
	
	public static SPanelElement getInstance()
	{
		if (instance == null)
			instance = new SPanelElement();
		
		return instance;
	}

	private SPanelElement()
	{		
		add(undo = new SToolBarButton(PersonalizedIcon.createImageIcon(
		    Slyum.ICON_PATH + "undo.png"), Slyum.ACTION_UNDO, Color.ORANGE,
		    TT_UNDO, this));
		
		add(redo = new SToolBarButton(PersonalizedIcon.createImageIcon(
		    Slyum.ICON_PATH + "redo.png"), Slyum.ACTION_REDO, Color.ORANGE,
		    TT_REDO, this));
		
    add(new SSeparator());

    add(createSButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "multinote.png"), Slyum.ACTION_NEW_NOTE_ASSOCIED,
        Color.CYAN, TT_ADD_NOTE, true));
    
    add(createSButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "color.png"), "Color", Color.CYAN,
        TT_CHANGE_COLOR, true));
    
    add(btnDelete = createSButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "delete.png"), "Delete", Color.CYAN,
        TT_DELETE, false));
    
    add(new SSeparator());

    add(alignTop = createEmptyButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "alignTop.png"), Slyum.ACTION_ALIGN_TOP,
        Color.GREEN, TT_ALIGN_TOP));
    
    add(alignBottom = createEmptyButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "alignBottom.png"), Slyum.ACTION_ALIGN_BOTTOM,
        Color.GREEN, TT_ALIGN_BOTTOM));
    
    add(alignRight = createEmptyButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "alignRight.png"), Slyum.ACTION_ALIGN_RIGHT,
        Color.GREEN, TT_ALIGN_RIGTH));
    
    add(alignLeft = createEmptyButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "alignLeft.png"), Slyum.ACTION_ALIGN_LEFT,
        Color.GREEN, TT_ALIGN_LEFT));
    
    add(adujst = createEmptyButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "adjustWidth.png"), Slyum.ACTION_ADJUST_WIDTH,
        Color.GREEN, TT_ADJUST_WIDTH));  
    
    add(new SSeparator());
    sliderZoom = new SSlider(100, 50, 200) {      
      @Override
      public void setValue(int value) {
        super.setValue(value);
        
        if (PanelClassDiagram.getInstance() != null)
          PanelClassDiagram.getInstance().getCurrentGraphicView().repaint();
      }
    };
    sliderZoom.setPreferredSize(new Dimension(100, getHeight()));
    add(sliderZoom);
    
    add(new SSeparator());
    
    add(top = createEmptyButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "top.png"), Slyum.ACTION_MOVE_TOP,
        Color.MAGENTA, TT_MOVE_TOP));
    
    add(up = createEmptyButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "up.png"), Slyum.ACTION_MOVE_UP,
        Color.MAGENTA, TT_MOVE_UP));
    
    add(down = createEmptyButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "down.png"), Slyum.ACTION_MOVE_DOWN,
        Color.MAGENTA, TT_MOVE_DOWN));
    
    add(bottom = createEmptyButton(PersonalizedIcon.createImageIcon(
        Slyum.ICON_PATH + "bottom.png"), Slyum.ACTION_MOVE_BOTTOM,
        Color.MAGENTA, TT_MOVE_BOTTOM));

    alignTop.setEnabled(false);
    alignBottom.setEnabled(false);
    alignRight.setEnabled(false);
    alignLeft.setEnabled(false);
    adujst.setEnabled(false);
		undo.setEnabled(false);
		redo.setEnabled(false);
	}
  
  private SButton createSButton(ImageIcon ii, String a, Color c, String tt, boolean enable)
  {
    SButton sb = new SToolBarButton(ii, a, c, tt, this);
    sb.setEnabled(enable);
    return sb;
  }

  private SButton createEmptyButton(ImageIcon ii, String action, Color c, String tt)
  {
    SButton ee = new SToolBarButton(ii, action, c, tt, this);
    ee.setEnabled(false);
    return ee;
  }

  public void componentSelectionChanged()
  {
    btnDelete.setEnabled(PanelClassDiagram.getInstance().getCurrentGraphicView().countSelectedComponents() > 0);
    updateBtnState();
  }
  
  public void updateBtnState()
  {
    GraphicView gv = PanelClassDiagram.getInstance().getCurrentGraphicView();
    int nb = gv.countSelectedEntities();
    boolean enable = nb > 1;
    alignTop.setEnabled(enable);
    alignBottom.setEnabled(enable);
    alignRight.setEnabled(enable);
    alignLeft.setEnabled(enable);
    
    enable = nb > 0;
    adujst.setEnabled(enable);

    enable = gv.countEntities() > 1 && gv.countSelectedEntities() > 0;
    top.setEnabled(enable);
    up.setEnabled(enable);
    down.setEnabled(enable);
    bottom.setEnabled(enable);
  }
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		GraphicView gv = PanelClassDiagram.getInstance().getCurrentGraphicView();
		gv.setStopRepaint(true);
		
		if (Slyum.ACTION_UNDO.equals(e.getActionCommand()))
			Change.undo();
		
		else if (Slyum.ACTION_REDO.equals(e.getActionCommand()))
			Change.redo();
    
		else if (e.getActionCommand().equals(Slyum.ACTION_DELETE))
      
      gv.deleteSelectedComponents();
    
    else if (e.getActionCommand().equals(Slyum.ACTION_COLOR))
      
      GraphicComponent.askNewColorForSelectedItems();
    
    else if (e.getActionCommand().equals(Slyum.ACTION_NEW_NOTE_ASSOCIED))
      
      gv.linkNewNoteWithSelectedEntities();
    
    else if (Slyum.ACTION_ALIGN_TOP.equals(e.getActionCommand()))
      gv.alignHorizontal(true);

    else if (Slyum.ACTION_ALIGN_BOTTOM.equals(e.getActionCommand()))
      gv.alignHorizontal(false);

    else if (Slyum.ACTION_ALIGN_LEFT.equals(e.getActionCommand()))
      gv.alignVertical(true);

    else if (Slyum.ACTION_ALIGN_RIGHT.equals(e.getActionCommand()))
      gv.alignVertical(false);

    else if (Slyum.ACTION_ADJUST_WIDTH.equals(e.getActionCommand()))
      gv.adjustWidthSelectedEntities();
    
    else if (Slyum.ACTION_MOVE_TOP.equals(e.getActionCommand()))
      
      gv.moveZOrderTopSelectedEntities();
    
    else if (Slyum.ACTION_MOVE_UP.equals(e.getActionCommand()))

      gv.moveZOrderUpSelectedEntities();
    
    else if (Slyum.ACTION_MOVE_DOWN.equals(e.getActionCommand()))

      gv.moveZOrderDownSelectedEntities();

    else if (Slyum.ACTION_MOVE_BOTTOM.equals(e.getActionCommand()))

      gv.moveZOrderBottomSelectedEntities();
		
		gv.goRepaint();
	}

	public SButton getUndoButton()
	{
		return undo;
	}
	
	public SButton getRedoButton()
	{
		return redo;
	}
  
  public SButton getBtnDelete()
  {
    return btnDelete;
  }
  
  public SButton getBtnAlignTop()
  {
    return alignTop;
  }
  
  public SButton getBtnAlignRight()
  {
    return alignRight;
  }
  
  public SButton getBtnAlignLeft()
  {
    return alignLeft;
  }
  
  public SButton getBtnAlignBottom()
  {
    return alignBottom;
  }
  
  public SButton getBtnAdjust()
  {
    return adujst;
  }
  
  public SButton getBtnTop()
  {
    return top;
  }
  
  public SButton getBtnUp()
  {
    return up;
  }
  
  public SButton getBtnDown()
  {
    return down;
  }
  
  public SButton getBtnBottom()
  {
    return bottom;
  }
  
  public SSlider getSliderZoom() {
    return sliderZoom;
  }
}
