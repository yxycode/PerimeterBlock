package com.perimeterblock;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.media.SoundPool;
import android.media.AudioManager;
import android.graphics.Typeface;
import java.util.*;

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
class Cell extends GameObject
{
public final static int CELL_WIDTH_PIXELS = 20;
public final static int CELL_HEIGHT_PIXELS = 20;
public static int Min_Id = 1, Max_Id = 10;

protected int PictureIndex, PictureRow, PictureColumn, AlphaValue;
public int Id, VisualGridX, VisualGridY;

public int SpecialFlag = FLAG_NORMAL;

public final static int FLAG_NORMAL = 0;
public final static int FLAG_RELATED_NEIGHBOR = 1;
public final static int FLAG_FINISHED_NEIGHBOR_TESTING = 2;
public final static int FLAG_DYING = 3;
public final static int FLAG_DEAD = 4;
public final static int FLAG_PARABOLIC_FLIGHT = 5;
public final static int FLAG_DEMO = 6;

public final static float FLIGHT_SPEED_MULTIPLIER = 5.0f;
public final static float FLIGHT_GRAVITY = 1.0f;
protected int DownwardAcceleration = 0, FlightAngle = 0;

public int WidthPercent = 100, HeightPercent = 100;

//-------------------------------------------------------------------------------------
public Cell( int nId, float fX , float fY )
{
super();
ClassType[TYPE_CELL] = 1;
PictureLayer = GE.LAYER_1;
AlphaValue = 255;
X = fX; Y = fY;
Id = nId;
PictureRow = GameOptions.TilesSelectionIndex;
PictureColumn = nId;

PictureIndex = GameControl.IMAGE_CELLS;
}
//-------------------------------------------------------------------------------------
public void Draw()
{
GameGlobals.DrawTileImageOne( PictureIndex, (int)X, (int)Y, PictureLayer, AlphaValue, PictureColumn, PictureRow,
	    	CELL_WIDTH_PIXELS, CELL_HEIGHT_PIXELS, WidthPercent, HeightPercent );
}
//-------------------------------------------------------------------------------------
public void CalculateGrid( int XGridShift, int YGridShift )
{
	VisualGridX = (int)(Math.floor(X/CELL_WIDTH_PIXELS)); VisualGridY = (int)(Math.floor(Y/CELL_HEIGHT_PIXELS));
	GridX = VisualGridX - XGridShift; GridY = VisualGridY - YGridShift;
}
//-------------------------------------------------------------------------------------
public void Do()
{
   if( SpecialFlag == FLAG_PARABOLIC_FLIGHT )
   {
	   DoParabolicFlight();
	   return;
   }
   else
   if( SpecialFlag == FLAG_DYING )
   {
	 AlphaValue = AlphaValue - 20;
	 if( AlphaValue <= 0 )
	 {	 
  	    AlphaValue = 0;
  	    SpecialFlag = FLAG_DEAD;
	 }
   }
   else
   if( SpecialFlag == FLAG_DEMO )
   {
	  PictureRow = GameOptions.TilesSelectionIndex;
   }
}
//-------------------------------------------------------------------------------------
public Cell GetCopy()
{
   Cell cell_obj = new Cell( Id, X, Y );
   cell_obj.VisualGridX = VisualGridX; cell_obj.VisualGridY = VisualGridY;
   cell_obj.GridX = GridX; cell_obj.GridY = GridY;
   cell_obj.AlphaValue = AlphaValue;
   
   return cell_obj;
}
//-------------------------------------------------------------------------------------
public void StartParabolicFlight()
{
 SpecialFlag = FLAG_PARABOLIC_FLIGHT;
 AlphaValue = 150;
 DownwardAcceleration = 0;
 FlightAngle = GameGlobals.random(190,340);
}
//-------------------------------------------------------------------------------------
public void DoParabolicFlight()
{
 X = X + FLIGHT_SPEED_MULTIPLIER * (float)Math.cos(Math.PI/180 * FlightAngle);
 Y = Y + FLIGHT_SPEED_MULTIPLIER * (float)Math.sin(Math.PI/180 * FlightAngle);
 Y = Y + DownwardAcceleration;
	   
 if( Y > GameEngine.TARGET_SCREEN_HEIGHT )
    SpecialFlag = FLAG_DEAD;
	   
 DownwardAcceleration += FLIGHT_GRAVITY;
}
//-------------------------------------------------------------------------------------
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
class CellOuterSpace extends GameObject
{
public final static int CELL_WIDTH_PIXELS = 20;
public final static int CELL_HEIGHT_PIXELS = 20;

protected int PictureIndex, PictureRow, PictureColumn, AlphaValue;	
protected int WidthPercent = 100, HeightPercent = 100;
public float Z;

//-------------------------------------------------------------------------------------	
public CellOuterSpace()
{
super();
ClassType[TYPE_CELL_OUTERSPACE] = 1;
PictureLayer = GE.LAYER_1;
AlphaValue = 255;
PictureRow = GameOptions.TilesSelectionIndex;
PictureColumn = GameGlobals.random( 1, 10 );
PictureIndex = GameControl.IMAGE_CELLS;
X = GameGlobals.random( 0, GE.TARGET_SCREEN_WIDTH ) - GE.TARGET_SCREEN_WIDTH/2;
Y = GameGlobals.random( 0, GE.TARGET_SCREEN_HEIGHT ) - GE.TARGET_SCREEN_HEIGHT/2;
Z = GameGlobals.random( 1, (int)(GE.TARGET_SCREEN_WIDTH/2 * 0.25) );
}
//-------------------------------------------------------------------------------------
protected void Respawn()
{
  X = GameGlobals.random( 0, GE.TARGET_SCREEN_WIDTH ) - GE.TARGET_SCREEN_WIDTH/2;
  Y = GameGlobals.random( 0, GE.TARGET_SCREEN_HEIGHT ) - GE.TARGET_SCREEN_HEIGHT/2;
  Z = GameGlobals.random( 1, (int)(GE.TARGET_SCREEN_WIDTH/2 * 0.25) );   
}
//-------------------------------------------------------------------------------------
public void Do()
{	
	Z = Z - 0.05f;
	if( Z < 1 )
        Respawn();
}
//-------------------------------------------------------------------------------------	
public void Draw()
{
int x3d = (int)(X/Z + GE.TARGET_SCREEN_WIDTH/2);
int y3d = (int)(Y/Z + GE.TARGET_SCREEN_HEIGHT/2);
AlphaValue = (int)((GE.TARGET_SCREEN_WIDTH/2 * 0.25 - Z) * 255/(GE.TARGET_SCREEN_WIDTH/2 * 0.25));

if( AlphaValue > 255 )
	AlphaValue = 255;
else
if( AlphaValue < 0 )
	AlphaValue = 0;

WidthPercent = HeightPercent = (int)(AlphaValue/2);

GameGlobals.DrawTileImageOne( PictureIndex, x3d, y3d, PictureLayer, AlphaValue, PictureColumn, PictureRow,
	    	CELL_WIDTH_PIXELS, CELL_HEIGHT_PIXELS, WidthPercent, HeightPercent );	
}
//-------------------------------------------------------------------------------------	
}
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
class StarsExplosion
{
public static final int MAX_STARS = 200;
public static final int MIN_STARS = 5;
public static final int MAX_FLIGHT_SPEED = 10;
public static final int MIN_FLIGHT_SPEED = 3;
public static final int MAX_LIFE_COUNT = 10;
public static final int CELL_WIDTH_PIXELS = 20;
public static final int CELL_HEIGHT_PIXELS = 20;

protected int StarCount = 0, LifeCount = 0;
protected int[] picture_column_index_list = new int[MAX_STARS];
protected float[] xlist = new float[MAX_STARS];
protected float[] ylist = new float[MAX_STARS];
protected int[] anglelist = new int[MAX_STARS];
protected float[] flightspeed = new float[MAX_STARS];
protected int[] sizepercent = new int[MAX_STARS];

//-------------------------------------------------------------------------------------
public StarsExplosion()
{
}
//-------------------------------------------------------------------------------------
public void Start( float x, float y )
{
  int i;
  
  LifeCount = MAX_LIFE_COUNT;
  StarCount = GameGlobals.random(MIN_STARS, MIN_STARS + 15);
  
  for( i = 0; i < StarCount; i++ )
  {
	 picture_column_index_list[i] = GameGlobals.random(0, 9);
	 xlist[i] = x; ylist[i] = y;
	 anglelist[i] = GameGlobals.random(0, 359);
	 flightspeed[i] = GameGlobals.random(MIN_FLIGHT_SPEED, MAX_FLIGHT_SPEED);
	 sizepercent[i] = GameGlobals.random(10, 150);
  }
}
//-------------------------------------------------------------------------------------
public void Start()
{
   LifeCount = MAX_LIFE_COUNT;
}
//-------------------------------------------------------------------------------------
public void Add( float x, float y )
{
   if( StarCount < MAX_STARS - 1 )
   {
	 picture_column_index_list[StarCount] = GameGlobals.random(0, 9);
	 xlist[StarCount] = x; ylist[StarCount] = y;
	 anglelist[StarCount] = GameGlobals.random(0, 359);
	 flightspeed[StarCount] = GameGlobals.random(MIN_FLIGHT_SPEED, MAX_FLIGHT_SPEED);
	 sizepercent[StarCount] = GameGlobals.random(10, 150);
	 
	 StarCount++;
   }
}
//-------------------------------------------------------------------------------------
public void Do()
{
  if( LifeCount <= 0 )
  {
	  StarCount = 0;
	  return;
  }
  
  for( int i = 0; i < StarCount; i++ )
  {
     xlist[i] += flightspeed[i] * (float)Math.cos(Math.PI/180 * anglelist[i]);
     ylist[i] += flightspeed[i] * (float)Math.sin(Math.PI/180 * anglelist[i]);
  }	
  
  LifeCount--;
  
}
//-------------------------------------------------------------------------------------
public void Draw()
{
  if( LifeCount <= 0 )
	  return;

  for( int i = 0; i < StarCount; i++ )
      GameGlobals.DrawTileImageOne( GameControl.IMAGE_STARS, (int)xlist[i], (int)ylist[i], 
    	 GameEngine.LAYER_3, 150, picture_column_index_list[i], 0,
	    	CELL_WIDTH_PIXELS, CELL_HEIGHT_PIXELS, sizepercent[i], sizepercent[i] );
}
//-------------------------------------------------------------------------------------

}
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
class CellFader
{
public static final int MAX_CELLS = 1000;

public static int picture_row_index = 0;

protected int[] picture_column_index_list = new int[MAX_CELLS];
protected int[] xlist = new int[MAX_CELLS];
protected int[] ylist = new int[MAX_CELLS];
protected int[] alphalist = new int[MAX_CELLS];

protected int AlphaCounter = 0;
public static final int MAX_ALPHA = 255;

public static final int CELL_WIDTH_PIXELS = 20, CELL_HEIGHT_PIXELS = 20;
public static final int STATUS_OFF = 0, STATUS_ON = 1, STATUS_DONE = 2;

public int StatusFlag = STATUS_OFF;

//-------------------------------------------------------------------------------------
public CellFader()
{
}
//-------------------------------------------------------------------------------------
public void AddCell( int picture_column_index , int nX, int nY )
{
  int i;
  for( i = 0; i < MAX_CELLS; i++ )
   if( picture_column_index_list[i] == 0 )
   {
      picture_column_index_list[i] = picture_column_index;
      xlist[i] = nX; ylist[i] = nY; 
      alphalist[i] = MAX_ALPHA;
      break;
   }
}
//-------------------------------------------------------------------------------------
public void AddCell( int picture_column_index , int nX, int nY, int alpha )
{
  int i;
  for( i = 0; i < MAX_CELLS; i++ )
   if( picture_column_index_list[i] == 0 )
   {
      picture_column_index_list[i] = picture_column_index;
      xlist[i] = nX; ylist[i] = nY; 
      alphalist[i] = alpha;
      break;
   }
}
//-------------------------------------------------------------------------------------
public void StartFade()
{
   AlphaCounter = MAX_ALPHA;
   StatusFlag = STATUS_ON;
}
//-------------------------------------------------------------------------------------
public void Do()
{
   int i;

   if( StatusFlag == STATUS_ON )
   {
     AlphaCounter -= 10;
     
     for( i = 0; i < MAX_CELLS; i++ )
      if( picture_column_index_list[i] > 0 )
      {
    	 if( alphalist[i] > 0 )
    		 alphalist[i] -= 10;
    	 else
    		 alphalist[i] = 0;
      }
   }
   
   if( AlphaCounter < 0 && StatusFlag == STATUS_ON )
   {
      AlphaCounter = 0;
      StatusFlag = STATUS_DONE;
      
      for( i = 0; i < MAX_CELLS; i++ )
          picture_column_index_list[i] = 0;
   }
}
//-------------------------------------------------------------------------------------
public void Draw()
{
  int i;
 
 if( StatusFlag == STATUS_ON )
  for( i = 0; i < MAX_CELLS; i++ )
   if( picture_column_index_list[i] >= 1 )
   {
       GameGlobals.DrawTileImageOne( GameControl.IMAGE_CELLS, xlist[i], ylist[i], 
         GameEngine.LAYER_3, alphalist[i], picture_column_index_list[i], picture_row_index, CELL_WIDTH_PIXELS, CELL_HEIGHT_PIXELS);	
   }
}
//-------------------------------------------------------------------------------------
}
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
class NextPieceButton extends GameObject
{
public final static int WIDTH_PIXELS = 48, HEIGHT_PIXELS = 48;
public final static int IMAGE_ROW_INDEX_MAX = 13;
public final static int ANIMATION_DELAY_MAX = 5;

public final static int[] ImageIndexList = { 0,1,2,3,4,5,6,7,6,5,4,3,2,1 };

protected int ImageRowIndex = 0;
protected int AnimationDelayCounter = 0;

protected Button ItsButton;
protected DragBoxGrid ItsDragBoxGrid;

public final static int STATUS_OFF = 0, STATUS_ON = 1;

public int StatusFlag = STATUS_ON;

public int TickCounter = 0;
public final static int TICK_COUNTER_MAX = 720;

//-------------------------------------------------------------------------------------	
public NextPieceButton( DragBoxGrid pDragBoxGrid )
{
super();
ClassType[TYPE_NEXT_PIECE_BUTTON] = 1;
MouseEventNotifyFlag = true;

X = GameEngine.TARGET_SCREEN_WIDTH/2 - WIDTH_PIXELS/2;
Y = DragBoxGrid.GRID_HEIGHT_CELLS/2 * DragBoxGrid.CELL_HEIGHT_PIXELS - HEIGHT_PIXELS/2;

ItsDragBoxGrid = pDragBoxGrid;

ItsButton = new Button( "#", 0,0,0,0 ); 
ItsButton.Create_WidthxHeight( (int)X , (int)Y, WIDTH_PIXELS, HEIGHT_PIXELS, GameGlobals.GROUP_ID_NONE, 
	 	GameGlobals.UNIQUE_ID_NONE, GE.LAYER_3, GE.LAYER_1, GameControl.IMAGE_NEXT_BUTTON, 
	 	ImageIndexList[ImageRowIndex], ImageIndexList[ImageRowIndex], 150 );
 
//ItsButton.PictureIndex0_TileX = GameOptions.NextButtonSelectionIndex;
//ItsButton.PictureIndex1_TileX = GameOptions.NextButtonSelectionIndex;
        InputTimeDelay = 300;
 
}
//-------------------------------------------------------------------------------------
public void OnClick()
{
  if( StatusFlag == STATUS_OFF )
	  return;
  
  ItsButton.OnClick();    
}
//-------------------------------------------------------------------------------------
public void Draw()
{
  if( StatusFlag == STATUS_OFF )
	  return;
  
	ItsButton.Draw();
}
//-------------------------------------------------------------------------------------
public void Do()
{

  if( StatusFlag == STATUS_OFF )
	  return;
  
  ItsButton.PictureIndex0_TileX = GameOptions.NextButtonSelectionIndex;
  ItsButton.PictureIndex1_TileX = GameOptions.NextButtonSelectionIndex;
  
  float xscale = (float)(Math.abs(Math.cos(TickCounter * 3.141594/180)));
		  
  ItsButton.ScaleXPercent =(int)(100 * xscale);
  ItsButton.ScaleYPercent = ItsButton.ScaleXPercent;
  ItsButton.X = X + (1.0f - xscale) * WIDTH_PIXELS/2;
  ItsButton.Y = Y + (1.0f - xscale) * HEIGHT_PIXELS/2;
  
  TickCounter += 2;
  if( TickCounter > TICK_COUNTER_MAX )
	  TickCounter = 0;
 
/*  
  if( AnimationDelayCounter < ANIMATION_DELAY_MAX )
	  AnimationDelayCounter++;
  else
  {
	  AnimationDelayCounter = 0;
	  ImageRowIndex++;
	  
	  if( ImageRowIndex > IMAGE_ROW_INDEX_MAX )
		  ImageRowIndex = 0;
	  
	  ItsButton.PictureIndex0_TileY = ImageIndexList[ImageRowIndex];
	  ItsButton.PictureIndex1_TileY = ImageIndexList[ImageRowIndex];
  }
 */ 
  
    if( ItsButton.MouseStatus_Dup == ItsButton.ME_PRESS_DOWN ||
		ItsButton.MouseStatus_Dup == ItsButton.ME_MOVE )
	{    
      if( ItsDragBoxGrid != null )
      {
    	ItsDragBoxGrid.SpawnDragBox();    	
    	StatusFlag = STATUS_OFF;
    	ItsDragBoxGrid.ItsStarsExplosion1.Start( GameEngine.TARGET_SCREEN_WIDTH/2 - StarsExplosion.CELL_WIDTH_PIXELS,
    			DragBoxGrid.GRID_HEIGHT_CELLS/2 * DragBoxGrid.CELL_HEIGHT_PIXELS - StarsExplosion.CELL_HEIGHT_PIXELS );
    	GameGlobals.PlaySound(3);
      }
	}
    else
    if( ItsButton.MouseStatus_Dup == ItsButton.ME_RELEASE )
    {
    	
    }
   
    ItsButton.ClearDupInput();
    ItsButton.Do();
    
}
//-------------------------------------------------------------------------------------
}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
class DragBoxGrid extends GameObject
{
public final static int GRID_WIDTH_CELLS = 16;
public final static int GRID_HEIGHT_CELLS = 20;	
public final static int CELL_WIDTH_PIXELS = 20;
public final static int CELL_HEIGHT_PIXELS = 20;
public final static int MAX_DRAG_BOXES = 10;

protected int[][] CellGrid = new int[GRID_WIDTH_CELLS][GRID_HEIGHT_CELLS];
protected int[][] ClearCellGrid = new int[GRID_WIDTH_CELLS][GRID_HEIGHT_CELLS];
protected int[][] CollideCellGrid = new int[GRID_WIDTH_CELLS][GRID_HEIGHT_CELLS];

protected DragBox[] DragBoxList = new DragBox[MAX_DRAG_BOXES];

protected String[] PerimeterGrid = new String[GRID_HEIGHT_CELLS];
protected String[] ShiftingGrid = new String[GRID_HEIGHT_CELLS];

protected NextPieceButton ItsNextPieceButton;

public final static int INACTIVITY_COUNTER_MAX = 25;
protected int InactivityCounter = 0;

public final static int MAX_PERIMETER_COUNT = 8;

public static String debugtext = "";

protected CellFader ItsCellFader1 = new CellFader();
protected CellFader ItsCellFader2 = new CellFader();
protected StarsExplosion ItsStarsExplosion1 = new StarsExplosion();
protected StarsExplosion ItsStarsExplosion2 = new StarsExplosion();

protected ScoreBar ItsScoreBar;

//-------------------------------------------------------------------------------------
public DragBoxGrid()
{
super();
ClassType[TYPE_DRAG_BOX_GRID] = 1;
MouseEventNotifyFlag = true;

ItsNextPieceButton = new NextPieceButton(this);
ItsNextPieceButton.StatusFlag = NextPieceButton.STATUS_OFF;

CellFader.picture_row_index = GameOptions.TilesSelectionIndex;

SpawnDragBox();

ShiftingGrid[0]  = "0000000000000000";
ShiftingGrid[1]  = "0qwwwwwwwwwwwwe0";
ShiftingGrid[2]  = "0aqwwwwwwwwwwed0";
ShiftingGrid[3]  = "0aaqwwwwwwwwedd0";
ShiftingGrid[4]  = "0aaaqwwwwwweddd0";
ShiftingGrid[5]  = "0aaaaqwwwwedddd0";
ShiftingGrid[6]  = "0aaaaaqwweddddd0";
ShiftingGrid[7]  = "0aaaaaaaddddddd0";
ShiftingGrid[8]  = "0aaaaaaaddddddd0";
ShiftingGrid[9]  = "0aaaaaaaddddddd0";
ShiftingGrid[10] = "0aaaaaaaddddddd0";
ShiftingGrid[11] = "0aaaaaaaddddddd0";
ShiftingGrid[12] = "0aaaaaaaddddddd0";
ShiftingGrid[13] = "0aaaaazxxcddddd0";
ShiftingGrid[14] = "0aaaazxxxxcdddd0";
ShiftingGrid[15] = "0aaazxxxxxxcaad0";
ShiftingGrid[16] = "0aazxxxxxxxxcad0";
ShiftingGrid[17] = "0azxxxxxxxxxxcd0";
ShiftingGrid[18] = "0zxxxxxxxxxxxxc0";
ShiftingGrid[19] = "0000000000000000";

PerimeterGrid[0]  = "0000000000000000";
PerimeterGrid[1]  = "0111111111111110";
PerimeterGrid[2]  = "0122222222222210";
PerimeterGrid[3]  = "0123333333333210";
PerimeterGrid[4]  = "0123444444443210";
PerimeterGrid[5]  = "0123455555543210";
PerimeterGrid[6]  = "0123456666543210";
PerimeterGrid[7]  = "0123456776543210";
PerimeterGrid[8]  = "0123456776543210";
PerimeterGrid[9]  = "0123456776543210";
PerimeterGrid[10] = "0123456776543210";
PerimeterGrid[11] = "0123456776543210";
PerimeterGrid[12] = "0123456776543210";
PerimeterGrid[13] = "0123456666543210";
PerimeterGrid[14] = "0123455555543210";
PerimeterGrid[15] = "0123444444443210";
PerimeterGrid[16] = "0123333333333210";
PerimeterGrid[17] = "0122222222222210";
PerimeterGrid[18] = "0111111111111110";
PerimeterGrid[19] = "0000000000000000";

}
//-------------------------------------------------------------------------------------
public void Init( ScoreBar sb )
{
  ItsScoreBar = sb;
}
//-------------------------------------------------------------------------------------
public void PasteDragBox( DragBox thedragbox )
{
 int x, y; float x0_center, y0_center;
 int grid_x0, grid_y0, grid_x1, grid_y1;
 
       x0_center = thedragbox.X + CELL_WIDTH_PIXELS/2;
       y0_center = thedragbox.Y + CELL_HEIGHT_PIXELS/2;
       
       grid_x0 = (int)(Math.floor(x0_center/CELL_WIDTH_PIXELS));
       grid_y0 = (int)(Math.floor(y0_center/CELL_HEIGHT_PIXELS));

       for( y = 0; y < DragBox.SHAPE_HEIGHT; y++ )
         for( x = 0; x < DragBox.SHAPE_WIDTH; x++ )
            if( thedragbox.ButtonGrid[x][y] != null )   
            {
              grid_x1 = grid_x0 + x;
              grid_y1 = grid_y0 + y;
              
              if( grid_x1 < 0 || grid_x1 > GRID_WIDTH_CELLS - 1 || grid_y1 < 0 || grid_y1 > GRID_HEIGHT_CELLS - 1 )
            	  continue;
              
              CellGrid[grid_x1][grid_y1] = thedragbox.ButtonGrid[x][y].PictureIndex0_TileX;
            }	

   int perimeter_count = CheckFormedPerimeter();
   int triplelength_count = CheckFormTriple();
		   
   thedragbox.StatusFlag = DragBox.STATUS_DEAD;
   
   //debugtext = "perimeter_count : " + perimeter_count;
   
   if( perimeter_count > 0 || triplelength_count > 0 )   
   {	  
	  Add2CellFaderClearCellGrid();
      ItsCellFader1.StartFade();       
      EraseClearCellGrid(); 
      
      ItsScoreBar.AddScore( perimeter_count * 1000 );
      ItsScoreBar.AddScore( triplelength_count * 10 );
      GameGlobals.PlaySound(2);
   }
   
}
//-------------------------------------------------------------------------------------
public int CheckCollideDragBox( DragBox thedragbox )
{
 int x, y; float x0_center, y0_center;
 int grid_x0, grid_y0, grid_x1, grid_y1;
 
       x0_center = thedragbox.X + CELL_WIDTH_PIXELS/2;
       y0_center = thedragbox.Y + CELL_HEIGHT_PIXELS/2;
       
       grid_x0 = (int)(Math.floor(x0_center/CELL_WIDTH_PIXELS));
       grid_y0 = (int)(Math.floor(y0_center/CELL_HEIGHT_PIXELS));

       for( y = 0; y < DragBox.SHAPE_HEIGHT; y++ )
         for( x = 0; x < DragBox.SHAPE_WIDTH; x++ )
            if( thedragbox.ButtonGrid[x][y] != null )   
            {
              grid_x1 = grid_x0 + x;
              grid_y1 = grid_y0 + y;
              
              if( grid_x1 < 0 || grid_x1 > GRID_WIDTH_CELLS - 1 || grid_y1 < 0 || grid_y1 > GRID_HEIGHT_CELLS - 1 )
            	  continue;
              
              if( CellGrid[grid_x1][grid_y1] >= 1 )
            	  return 1;
            }	
       
       return 0;
}
//-------------------------------------------------------------------------------------
public void Draw()
{
  int x, y, i;
  
for( y = 0; y < GRID_HEIGHT_CELLS; y++ )
 for( x = 0; x < GRID_WIDTH_CELLS; x++ )	 
   if( CellGrid[x][y] >= 1 )	
   {   
       GameGlobals.DrawTileImageOne( GameControl.IMAGE_CELLS, (int)(X + x * CELL_WIDTH_PIXELS), (int)(Y + y * CELL_HEIGHT_PIXELS), 
	      GE.LAYER_3, 255, CellGrid[x][y], GameOptions.TilesSelectionIndex, CELL_WIDTH_PIXELS, CELL_HEIGHT_PIXELS);		 
   }

for( i = 0; i < MAX_DRAG_BOXES; i++ )
	if( DragBoxList[i] != null )
		DragBoxList[i].Draw();	

ItsNextPieceButton.Draw();
ItsCellFader1.Draw();
ItsCellFader2.Draw();
ItsStarsExplosion1.Draw();
ItsStarsExplosion2.Draw();

//GE.DrawTextColor( debugtext, 5, 480-20, GE.LAYER_4, Color.rgb(0,255,0), 16 );

}
//-------------------------------------------------------------------------------------
public void Do()
{
int i, dbcount = 0, deadcount = 0;
int perimeter_count, triplelength_count;

   ItsCellFader2.Do();
   
   if( ItsCellFader1.StatusFlag == CellFader.STATUS_ON )
   {	   
      ItsCellFader1.Do();
      //return;
   }
   else
   if( ItsCellFader1.StatusFlag == CellFader.STATUS_DONE )
   {
      ItsCellFader1.StatusFlag = CellFader.STATUS_OFF;
      
      for( i = 0; i < MAX_DRAG_BOXES; i++ )
	    if( DragBoxList[i] != null )
	     if( DragBoxList[i].PressedFlag == 0 && DragBoxList[i].GetLife() != DragBox.MAX_LIVES )
	        PasteDragBox( DragBoxList[i]);

          ShiftBlocksOutward();

          perimeter_count = CheckFormedPerimeter();
          triplelength_count = CheckFormTriple();
		   
          if( perimeter_count > 0 || triplelength_count > 0 )   
          {	  
	       Add2CellFaderClearCellGrid();
           ItsCellFader1.StartFade();       
           EraseClearCellGrid(); 

           ItsScoreBar.AddScore( perimeter_count * 1000 );
           ItsScoreBar.AddScore( triplelength_count * 10 );           
          }              
       //debugtext = "count: " + perimeter_count  + ", " +  triplelength_count;
   }
   

  if( InactivityCounter < INACTIVITY_COUNTER_MAX )
      InactivityCounter++;
  else
  {
	  InactivityCounter = 0;
	  ItsNextPieceButton.StatusFlag = NextPieceButton.STATUS_ON;
  }
  
  if( ItsNextPieceButton.StatusFlag == NextPieceButton.STATUS_ON )
	  InactivityCounter = 0;

for( i = 0; i < MAX_DRAG_BOXES; i++ )
	if( DragBoxList[i] != null )
	{		
		DragBoxList[i].Do();	
						
		if( DragBoxList[i].StatusFlag == DragBox.STATUS_DEAD )
		{	
			DragBoxList[i] = null;
			deadcount++;
			continue;
		} 
		dbcount++;
	}

if( dbcount <= 0 )
	SpawnDragBox();

ItsNextPieceButton.Do();
ItsStarsExplosion1.Do();
ItsStarsExplosion2.Do();
}
//-------------------------------------------------------------------------------------
public void OnClick()
{
int i;
/*
if( CellFader.StatusFlag == CellFader.STATUS_ON )
	return;
*/
for( i = 0; i < MAX_DRAG_BOXES; i++ )
	if( DragBoxList[i] != null )
		DragBoxList[i].OnClick();

ItsNextPieceButton.OnClick();

}
//-------------------------------------------------------------------------------------
public void SpawnDragBox()
{
int i;

for( i = 0; i < MAX_DRAG_BOXES; i++ )
	if( DragBoxList[i] == null )
	{
      DragBoxList[i] = new DragBox( GameEngine.TARGET_SCREEN_WIDTH/2 - DragBox.SHAPE_WIDTH/2 * CELL_WIDTH_PIXELS, 
    		  GRID_HEIGHT_CELLS/2 * CELL_HEIGHT_PIXELS - DragBox.SHAPE_WIDTH/2 * CELL_HEIGHT_PIXELS, this );
      DragBoxList[i].SetLife(9);
      break;
	}


InactivityCounter = 0; 
ItsNextPieceButton.StatusFlag = NextPieceButton.STATUS_OFF;
}
//-------------------------------------------------------------------------------------
protected int CheckFormedPerimeter()
{
  int x, y, i, max_perimeter_unit_count, perimeter_unit_count, total_perimeter_formed_count = 0;
  int c;

  for( i = 0; i < MAX_PERIMETER_COUNT; i++ )
  {
   perimeter_unit_count = 0;
   max_perimeter_unit_count = 0;

   for( y = 0; y < GRID_HEIGHT_CELLS; y++ )
    for( x = 0; x < GRID_WIDTH_CELLS; x++ )
    {
     c = PerimeterGrid[y].charAt(x);
     c = c - '0';

     if( c == i )
     {
       max_perimeter_unit_count++;

       if( CellGrid[x][y] > 0 )
           perimeter_unit_count++;
     }
    } 

    if( perimeter_unit_count >= max_perimeter_unit_count )
    {
      for( y = 0; y < GRID_HEIGHT_CELLS; y++ )
       for( x = 0; x < GRID_WIDTH_CELLS; x++ )
       {
         c = PerimeterGrid[y].charAt(x);
         c = c - '0';

         if( c == i )                   
            ClearCellGrid[x][y] = CellGrid[x][y];         
       }  
      
     total_perimeter_formed_count++;
   }
}
   return total_perimeter_formed_count;
}
//-------------------------------------------------------------------------------------
protected void ShiftBlocksOutward()
{
   int x, y, c, i;
   int perimeter_index, perimeter_alpha;

 for( i = 0; i < MAX_PERIMETER_COUNT; i++ )
   for( y = 0; y < GRID_HEIGHT_CELLS; y++ )
    for( x = 0; x < GRID_WIDTH_CELLS; x++ )
    {
      c = ShiftingGrid[y].charAt(x);
      perimeter_index = MAX_PERIMETER_COUNT - (PerimeterGrid[y].charAt(x) - '0');
      perimeter_alpha = perimeter_index * CellFader.MAX_ALPHA/MAX_PERIMETER_COUNT;
      
      switch(c)
      {
        case '0':
          break;
        case 'q':
          if( CellGrid[x][y-1] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );        	  
            CellGrid[x][y-1] = CellGrid[x][y];
            CellGrid[x][y] = 0;            
          }
          else
          if( CellGrid[x-1][y-1] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );      
            CellGrid[x-1][y-1] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          else
          if( CellGrid[x-1][y] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );      
            CellGrid[x-1][y] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          
          break;
        case 'a':
          if( CellGrid[x-1][y] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS,  perimeter_alpha );        
            CellGrid[x-1][y] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          break;
        case 'w':
          if( CellGrid[x][y-1] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );      
            CellGrid[x][y-1] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          break;
        case 'd':
          if( CellGrid[x+1][y] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );
            CellGrid[x+1][y] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          break;
        case 'e':
          if( CellGrid[x][y-1] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha ); 
            CellGrid[x][y-1] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          else
          if( CellGrid[x+1][y-1] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );  
            CellGrid[x+1][y-1] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          else
          if( CellGrid[x+1][y] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );  
            CellGrid[x+1][y] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          break;
        case 'z':
          if( CellGrid[x][y+1] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );  
            CellGrid[x][y+1] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          else
          if( CellGrid[x-1][y+1] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );  
            CellGrid[x-1][y+1] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          else
          if( CellGrid[x-1][y] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );  
            CellGrid[x-1][y] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          break;
        case 'x':
          if( CellGrid[x][y+1] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );  
            CellGrid[x][y+1] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          break;
        case 'c':
          if( CellGrid[x][y+1] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );  
            CellGrid[x][y+1] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          else
          if( CellGrid[x+1][y+1] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );  
            CellGrid[x+1][y+1] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          else
          if( CellGrid[x+1][y] == 0 )
          {
        	ItsCellFader2.AddCell( CellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, perimeter_alpha );  
            CellGrid[x+1][y] = CellGrid[x][y];
            CellGrid[x][y] = 0;
          }
          break;
        default:
          break;
      }
    }
 
    ItsCellFader2.StartFade();
    GameGlobals.PlaySound(0);

}
//-------------------------------------------------------------------------------------
public void EraseClearCellGrid()
{
  int x, y;
  
  for( y = 0; y < GRID_HEIGHT_CELLS; y++ )
   for( x = 0; x < GRID_WIDTH_CELLS; x++ )
     ClearCellGrid[x][y] = 0;
}
//-------------------------------------------------------------------------------------
protected int CheckCellsEqual( int[] xlist, int[] ylist, int list_length )
{
  int i, value = CellGrid[ xlist[0] ][ ylist[0] ];
  
  if( value == 0 )
	  return 0;
  
  for( i = 1; i < list_length; i++ )
    if( CellGrid[ xlist[i] ][ ylist[i] ] == value )
       ; // null statement
    else
       return 0;
       
  return 1;
}
//-------------------------------------------------------------------------------------
protected int CheckWithinGrid( int[] xlist, int[] ylist, int list_length )
{
  int i;
  for( i = 0; i < list_length; i++ )
    if( 0 <= xlist[i] && xlist[i] <= GRID_WIDTH_CELLS - 1 &&
        0 <= ylist[i] && ylist[i] <= GRID_HEIGHT_CELLS - 1 )   
       ; // null statement
    else
       return 0;
       
  return 1;
}
//-------------------------------------------------------------------------------------
public int CheckFormTriple()
{
  int SEGMENT_LENGTH = 3;
  int loop_x, loop_y, i, clear_count = 0;
  int[] x = new int[SEGMENT_LENGTH];
  int[] y = new int[SEGMENT_LENGTH];
  
for( loop_y = 0; loop_y < GRID_HEIGHT_CELLS; loop_y++ )
 for( loop_x = 0; loop_x < GRID_WIDTH_CELLS; loop_x++ )	
 {
    x[0] = -1; y[0] =  0; 
    x[1] =  0; y[1] =  0;
    x[2] =  0; y[2] = -1;
    
    x[0] += loop_x; y[0] += loop_y; x[1] += loop_x; y[1] += loop_y; x[2] += loop_x; y[2] += loop_y;
 
   if( CheckWithinGrid( x, y, SEGMENT_LENGTH ) >= 1 )
    if( CheckCellsEqual( x, y, SEGMENT_LENGTH ) >= 1 )
    {
     for( i = 0; i < SEGMENT_LENGTH; i++ )    
      ClearCellGrid[x[i]][y[i]] = CellGrid[loop_x][loop_y];    
     clear_count++;
    }  
    x[0] = 1; y[0] =  0; 
    x[1] =  0; y[1] =  0;
    x[2] =  0; y[2] = -1;
    
    x[0] += loop_x; y[0] += loop_y; x[1] += loop_x; y[1] += loop_y; x[2] += loop_x; y[2] += loop_y;
 
   if( CheckWithinGrid( x, y, SEGMENT_LENGTH ) >= 1 )
    if( CheckCellsEqual( x, y, SEGMENT_LENGTH ) >= 1 )
    {
     for( i = 0; i < SEGMENT_LENGTH; i++ )    
      ClearCellGrid[x[i]][y[i]] = CellGrid[loop_x][loop_y];   
     clear_count++;
    }
 
    x[0] = -1; y[0] =  0; 
    x[1] =  0; y[1] =  0;
    x[2] =  0; y[2] = 1;
    
    x[0] += loop_x; y[0] += loop_y; x[1] += loop_x; y[1] += loop_y; x[2] += loop_x; y[2] += loop_y;
 
   if( CheckWithinGrid( x, y, SEGMENT_LENGTH ) >= 1 )
    if( CheckCellsEqual( x, y, SEGMENT_LENGTH ) >= 1 )
    {
     for( i = 0; i < SEGMENT_LENGTH; i++ )    
       ClearCellGrid[x[i]][y[i]] = CellGrid[loop_x][loop_y];           
     clear_count++;
    }
    
    x[0] =  1; y[0] =  0; 
    x[1] =  0; y[1] =  0;
    x[2] =  0; y[2] = 1;
    
   x[0] += loop_x; y[0] += loop_y; x[1] += loop_x; y[1] += loop_y; x[2] += loop_x; y[2] += loop_y;
 
   if( CheckWithinGrid( x, y, SEGMENT_LENGTH ) >= 1 )
    if( CheckCellsEqual( x, y, SEGMENT_LENGTH ) >= 1 )
    {
     for( i = 0; i < SEGMENT_LENGTH; i++ )    
      ClearCellGrid[x[i]][y[i]] = CellGrid[loop_x][loop_y];  
     clear_count++;
    }

    x[0] = -1; y[0] =  0; 
    x[1] =  0; y[1] =  0;
    x[2] =  1; y[2] =  0;
    
    x[0] += loop_x; y[0] += loop_y; x[1] += loop_x; y[1] += loop_y; x[2] += loop_x; y[2] += loop_y;
 
   if( CheckWithinGrid( x, y, SEGMENT_LENGTH ) >= 1 )
    if( CheckCellsEqual( x, y, SEGMENT_LENGTH ) >= 1 )
    {
     for( i = 0; i < SEGMENT_LENGTH; i++ )    
      ClearCellGrid[x[i]][y[i]] = CellGrid[loop_x][loop_y];  
     clear_count++;
    }
    
    x[0] =  0; y[0] =  -1; 
    x[1] =  0; y[1] =  0;
    x[2] =  0; y[2] =  1;
    
    x[0] += loop_x; y[0] += loop_y; x[1] += loop_x; y[1] += loop_y; x[2] += loop_x; y[2] += loop_y;
 
   if( CheckWithinGrid( x, y, SEGMENT_LENGTH ) >= 1 )
    if( CheckCellsEqual( x, y, SEGMENT_LENGTH ) >= 1 )
    {
     for( i = 0; i < SEGMENT_LENGTH; i++ )    
      ClearCellGrid[x[i]][y[i]] = CellGrid[loop_x][loop_y];          
     clear_count++;
    } 

    x[0] =  -1; y[0] =  -1; 
    x[1] =  0; y[1] =  0;
    x[2] =  1; y[2] =  1;
    
    x[0] += loop_x; y[0] += loop_y; x[1] += loop_x; y[1] += loop_y; x[2] += loop_x; y[2] += loop_y;
 
   if( CheckWithinGrid( x, y, SEGMENT_LENGTH ) >= 1 )
    if( CheckCellsEqual( x, y, SEGMENT_LENGTH ) >= 1 )
    {
     for( i = 0; i < SEGMENT_LENGTH; i++ )    
      ClearCellGrid[x[i]][y[i]] = CellGrid[loop_x][loop_y];        
     clear_count++;
    }
    
    x[0] =  -1; y[0] =  1; 
    x[1] =  0; y[1] =  0;
    x[2] =  1; y[2] =  -1;
    
    x[0] += loop_x; y[0] += loop_y; x[1] += loop_x; y[1] += loop_y; x[2] += loop_x; y[2] += loop_y;
 
   if( CheckWithinGrid( x, y, SEGMENT_LENGTH ) >= 1 )
    if( CheckCellsEqual( x, y, SEGMENT_LENGTH ) >= 1 )
    {
     for( i = 0; i < SEGMENT_LENGTH; i++ )    
      ClearCellGrid[x[i]][y[i]] = CellGrid[loop_x][loop_y];       
     clear_count++;
    }
 }
   return clear_count;
}
//-------------------------------------------------------------------------------------
public void Add2CellFaderClearCellGrid()
{
  int x, y;
  
  for( y = 0; y < GRID_HEIGHT_CELLS; y++ )
   for( x = 0; x < GRID_WIDTH_CELLS; x++ )	   
	if( ClearCellGrid[x][y] > 0 )
	{
      ItsCellFader1.AddCell( ClearCellGrid[x][y], x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS );  
      CellGrid[x][y] = 0;
      ItsStarsExplosion2.Add( x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS );            
	}
    ItsStarsExplosion2.Start();
}
//-------------------------------------------------------------------------------------
protected int CheckCollideDragBoxList()
{
  int x, y, i, collide_count = 0;
  
  for( y = 0; y < GRID_HEIGHT_CELLS; y++ )
   for( x = 0; x < GRID_WIDTH_CELLS; x++ )	  
      CollideCellGrid[x][y] = 0;
  
  for( i = 0; i < MAX_DRAG_BOXES; i++ )
     if( DragBoxList[i] != null )
    	 PasteDragBoxCollideGrid( DragBoxList[i] );
  
  for( i = 0; i < MAX_DRAG_BOXES; i++ )
     if( DragBoxList[i] != null )
     {
      if( CheckDragBoxCollideGrid( DragBoxList[i] ) >= 1 )
      {
    	  DragBoxList[i].GridCollideFlag = 1;
    	  collide_count++;   
      }
/*
     else
        DragBoxList[i].GridCollideFlag = 0;     
 */
     }
  
   int breakflag = 0;
   
   if( ItsNextPieceButton.StatusFlag == NextPieceButton.STATUS_ON )
   {
    for( y = 0; y < GRID_HEIGHT_CELLS; y++ )
    { 	
     for( x = 0; x < GRID_WIDTH_CELLS; x++ )	  
       if( CollideCellGrid[x][y] >= 1 )
       {   
    	 if( GameGlobals.CheckRectangleCollide( x * CELL_WIDTH_PIXELS, y * CELL_HEIGHT_PIXELS, 
    			   CELL_WIDTH_PIXELS, CELL_HEIGHT_PIXELS,
    			   ItsNextPieceButton.X, ItsNextPieceButton.Y, NextPieceButton.WIDTH_PIXELS, NextPieceButton.HEIGHT_PIXELS ) >= 1 )
    	 {    		 
    		 //debugtext = "collide: " + x + "," + y;
    		 ItsNextPieceButton.StatusFlag = NextPieceButton.STATUS_OFF;
    		 InactivityCounter = 0;
    		 breakflag = 1;
    		 break;
    	 }
       }      
       if( breakflag >= 1 )
    	   break;
     }
   }
  
  return collide_count;
}
//-------------------------------------------------------------------------------------
protected void PasteDragBoxCollideGrid( DragBox thedragbox )
{
 int x, y; float x0_center, y0_center;
 int grid_x0, grid_y0, grid_x1, grid_y1;
 
       x0_center = thedragbox.X + CELL_WIDTH_PIXELS/2;
       y0_center = thedragbox.Y + CELL_HEIGHT_PIXELS/2;
       
       grid_x0 = (int)(Math.floor(x0_center/CELL_WIDTH_PIXELS));
       grid_y0 = (int)(Math.floor(y0_center/CELL_HEIGHT_PIXELS));

       for( y = 0; y < DragBox.SHAPE_HEIGHT; y++ )
         for( x = 0; x < DragBox.SHAPE_WIDTH; x++ )
            if( thedragbox.ButtonGrid[x][y] != null )   
            {
              grid_x1 = grid_x0 + x;
              grid_y1 = grid_y0 + y;
              
              if( grid_x1 < 0 || grid_x1 > GRID_WIDTH_CELLS - 1 || grid_y1 < 0 || grid_y1 > GRID_HEIGHT_CELLS - 1 )
            	  continue;
              
               CollideCellGrid[grid_x1][grid_y1] += 1;
   
            }	
}
//-------------------------------------------------------------------------------------
protected int CheckDragBoxCollideGrid( DragBox thedragbox )
{
 int x, y; float x0_center, y0_center;
 int grid_x0, grid_y0, grid_x1, grid_y1;
 
       x0_center = thedragbox.X + CELL_WIDTH_PIXELS/2;
       y0_center = thedragbox.Y + CELL_HEIGHT_PIXELS/2;
       
       grid_x0 = (int)(Math.floor(x0_center/CELL_WIDTH_PIXELS));
       grid_y0 = (int)(Math.floor(y0_center/CELL_HEIGHT_PIXELS));

       for( y = 0; y < DragBox.SHAPE_HEIGHT; y++ )
         for( x = 0; x < DragBox.SHAPE_WIDTH; x++ )
            if( thedragbox.ButtonGrid[x][y] != null )   
            {
              grid_x1 = grid_x0 + x;
              grid_y1 = grid_y0 + y;
              
              if( grid_x1 < 0 || grid_x1 > GRID_WIDTH_CELLS - 1 || grid_y1 < 0 || grid_y1 > GRID_HEIGHT_CELLS - 1 )
            	  continue;
              
               if( CollideCellGrid[grid_x1][grid_y1] >= 2 )
            	   return 1;
   
            }	
       return 0;
}
//-------------------------------------------------------------------------------------
}
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
class DragBox extends GameObject
{
public final static int GRID_WIDTH_CELLS = DragBoxGrid.GRID_WIDTH_CELLS;
public final static int GRID_HEIGHT_CELLS = DragBoxGrid.GRID_HEIGHT_CELLS;	

public final static int SHAPE_WIDTH = 5, SHAPE_HEIGHT = 5;
public final static int CELL_WIDTH_PIXELS = 20;
public final static int CELL_HEIGHT_PIXELS = 20;
public final static int MAX_LIVES = 9;

Button[][] ButtonGrid = new Button[SHAPE_WIDTH][SHAPE_HEIGHT];
int[][] ButtonOverlay = new int[SHAPE_WIDTH][SHAPE_HEIGHT];

protected float RelativeClickX, RelativeClickY, X_Old, Y_Old;

public final static int MAX_SHAPES = 15;
public static int ShapesCap = 11;

protected int PressReleaseCount = 0;
protected int ReleaseDurationCounter = 15, ReleaseDurationCounterMax = 15;
protected int Snap2GridCounter = 0, Snap2GridCounterMax = 10;
protected int LifeCounter = 0, LifeCounterMax = 50;

public static DragBoxGrid ItsDragBoxGrid;
protected int GridCollideFlag = 0;
protected int DragBoxCollideFlag = 0;

public final static int STATUS_ALIVE = 1;
public final static int STATUS_DEAD = 0;

public int StatusFlag = STATUS_ALIVE;
public int PressedFlag = 0;

//-------------------------------------------------------------------------------------
public DragBox( float fX, float fY, DragBoxGrid pDragBoxGrid )
{
super();
ClassType[TYPE_DRAG_BOX] = 1;
MouseEventNotifyFlag = true;
X = fX; Y = fY;

 ItsDragBoxGrid = pDragBoxGrid;
 
 GenerateShape( GameGlobals.random(0, ShapesCap - 1) );

 RelativeClickX = RelativeClickY = -1;
}
//-------------------------------------------------------------------------------------
public void GenerateShape( int index )
{
final int MAX_COLORS = 5; // 10;

String[][] slist = new String[MAX_SHAPES][SHAPE_HEIGHT];

slist[0][0] = ".....";
slist[0][1] = "..#..";
slist[0][2] = "..##.";
slist[0][3] = ".....";
slist[0][4] = ".....";

slist[1][0] = ".....";
slist[1][1] = "..#..";
slist[1][2] = ".##..";
slist[1][3] = ".....";
slist[1][4] = ".....";

slist[2][0] = ".....";
slist[2][1] = "..#..";
slist[2][2] = "..#..";
slist[2][3] = ".....";
slist[2][4] = ".....";

slist[3][0] = ".....";
slist[3][1] = "...#.";
slist[3][2] = "..##.";
slist[3][3] = "..##.";
slist[3][4] = ".....";

slist[4][0] = ".....";
slist[4][1] = ".....";
slist[4][2] = ".###.";
slist[4][3] = ".##..";
slist[4][4] = ".....";

slist[5][0] = ".....";
slist[5][1] = "..##.";
slist[5][2] = "..#..";
slist[5][3] = "..##.";
slist[5][4] = ".....";

slist[6][0] = ".....";
slist[6][1] = ".....";
slist[6][2] = "#####";
slist[6][3] = ".....";
slist[6][4] = ".....";

slist[7][0] = ".....";
slist[7][1] = ".#.#.";
slist[7][2] = "..#..";
slist[7][3] = ".....";
slist[7][4] = ".....";

slist[8][0] = ".....";
slist[8][1] = ".###.";
slist[8][2] = ".###.";
slist[8][3] = ".....";
slist[8][4] = ".....";

slist[9][0] = ".....";
slist[9][1] = ".###.";
slist[9][2] = ".###.";
slist[9][3] = ".###.";
slist[9][4] = ".....";

slist[10][0] = ".....";
slist[10][1] = "..#..";
slist[10][2] = "..#..";
slist[10][3] = "..#..";
slist[10][4] = ".....";

slist[11][0] = ".....";
slist[11][1] = ".....";
slist[11][2] = ".....";
slist[11][3] = ".....";
slist[11][4] = ".....";

slist[12][0] = ".....";
slist[12][1] = ".....";
slist[12][2] = ".....";
slist[12][3] = ".....";
slist[12][4] = ".....";

slist[13][0] = ".....";
slist[13][1] = ".....";
slist[13][2] = ".....";
slist[13][3] = ".....";
slist[13][4] = ".....";

slist[14][0] = ".....";
slist[14][1] = ".....";
slist[14][2] = ".....";
slist[14][3] = ".....";
slist[14][4] = ".....";


Button but;
int x, y, cell_color, onceflag = 0;

for( y = 0; y < SHAPE_HEIGHT; y++ )
 for( x = 0; x < SHAPE_WIDTH; x++ )
 if( slist[index][y].charAt(x) == '#' )
 {
   if( onceflag == 0 )	 
   {
      ButtonOverlay[x][y] = MAX_LIVES;
      onceflag = 1;
   }
   
   cell_color = GameGlobals.random( 1, MAX_COLORS );
   
   but = new Button( "#", 0,0,0,0 ); 
   but.Create_WidthxHeight( (int)(X + x * CELL_WIDTH_PIXELS), (int)(Y + y * CELL_HEIGHT_PIXELS), CELL_WIDTH_PIXELS, CELL_HEIGHT_PIXELS, GameGlobals.GROUP_ID_NONE, 
	 	GameGlobals.UNIQUE_ID_NONE, GE.LAYER_2, GE.LAYER_1, GameControl.IMAGE_CELLS, 0, 0, 255 );
   but.PictureIndex0_TileX = cell_color; //index % MAX_COLORS + 1;
   but.PictureIndex1_TileX = cell_color; //index % MAX_COLORS + 1;;
   but.PictureIndex0_TileY = GameOptions.TilesSelectionIndex;
   but.PictureIndex1_TileY = GameOptions.TilesSelectionIndex;
   but.ReleaseType = but.UNCLICK_WHEN_RELEASE;
   but.InputTimeDelay = 150;
   ButtonGrid[x][y] = but;
 }
 else
   ButtonGrid[x][y] = null;

}
//-------------------------------------------------------------------------------------
public void OnClick()
{
int x, y;
//float[][][] return_xy = new float[2][SHAPE_WIDTH][SHAPE_HEIGHT];

for( y = 0; y < SHAPE_HEIGHT; y++ )
 for( x = 0; x < SHAPE_WIDTH; x++ )
	if( ButtonGrid[x][y] != null )
		ButtonGrid[x][y].OnClick();
}
//-------------------------------------------------------------------------------------
public void Do()
{
  int x, y, x1, y1, UniqueId, breakloopflag = 0;  
  Button but;
  float[][][] return_xy = new float[2][SHAPE_WIDTH][SHAPE_HEIGHT]; 
  
  if( ReleaseDurationCounter < ReleaseDurationCounterMax )
	 ReleaseDurationCounter++;
  else	
     PressReleaseCount = 0;
  
  if( Snap2GridCounter < Snap2GridCounterMax )
	  Snap2GridCounter++;
  else
  if( Snap2GridCounter >= Snap2GridCounterMax )
  {	  
	Snap2GridCounter = 0;
	Snap2Grid();
	RelativeClickX = RelativeClickY = -1;
  }
  
  //GameGlobals.debugcaption.SetText("" + ReleaseDurationCounter + ", " + PressReleaseCount);
    
  if( LifeCounter < LifeCounterMax )
	  LifeCounter++;
  else
  {
	  LifeCounter = 0;
	  DecreaseLife();
	  
	  if( GetLife() <= 0 )
	  {
		if( GridCollideFlag == 0 && DragBoxCollideFlag == 0 )
		{		
		 ItsDragBoxGrid.PasteDragBox(this);
		 //StatusFlag = STATUS_DEAD;
	    } 
		else
		{
		  //SetLife(MAX_LIVES);
		  ScoreBar.CurrentGameState = ScoreBar.GAME_STATE_GAME_OVER_MESSAGE_BEGIN;
		}
	  }
  }
  
  
  DragBoxCollideFlag = ItsDragBoxGrid.CheckCollideDragBoxList();
  GridCollideFlag = CheckCollideGrid();
  
for( y = 0; y < SHAPE_HEIGHT; y++ )
{	
 for( x = 0; x < SHAPE_WIDTH; x++ )
   if( ButtonGrid[x][y] != null )
   {
    UniqueId = ButtonGrid[x][y].UniqueId;
    but = ButtonGrid[x][y];
    but.Do();
	   
    if( but.MouseStatus_Dup == but.ME_PRESS_DOWN ||
		but.MouseStatus_Dup == but.ME_MOVE )
	{               
       SetPressedFlag(1);
       Snap2GridCounter = 0;
       ItsDragBoxGrid.ItsNextPieceButton.StatusFlag = NextPieceButton.STATUS_OFF;
       ItsDragBoxGrid.InactivityCounter = 0;
       
       if( RelativeClickX <= -1 || RelativeClickY <= -1 )
       {
    	   RelativeClickX = MouseX - X; RelativeClickY = MouseY - Y;
    	   //breakloopflag = 1;
    	   //break;
       }
       
       X = MouseX - RelativeClickX;
       Y = MouseY - RelativeClickY;

       for( y = 0; y < SHAPE_HEIGHT; y++ )
         for( x = 0; x < SHAPE_WIDTH; x++ )
            if( ButtonGrid[x][y] != null )   
            {
              ButtonGrid[x][y].X = X + x * CELL_WIDTH_PIXELS;
              ButtonGrid[x][y].Y = Y + y * CELL_HEIGHT_PIXELS;  
              ButtonGrid[x][y].ClearDupInput();
            }      
       
       breakloopflag = 1;       
       break;
	}
    else
    if( but.MouseStatus_Dup == but.ME_RELEASE )
    {
       //SetPressedFlag(0);
       ReleaseDurationCounter = 0;	
       PressReleaseCount++;
       
       ItsDragBoxGrid.ItsNextPieceButton.StatusFlag = NextPieceButton.STATUS_OFF;
       ItsDragBoxGrid.InactivityCounter = 0;       
       
       if( PressReleaseCount >= 2 )
       {
    	  RotateRight();
    	  PressReleaseCount = 0;
    	  PressedFlag = 0;
       }
       
       for( y = 0; y < SHAPE_HEIGHT; y++ )
         for( x = 0; x < SHAPE_WIDTH; x++ )
            if( ButtonGrid[x][y] != null )   
                ButtonGrid[x][y].ClearDupInput();
       
        RelativeClickX = RelativeClickY = -1;
        breakloopflag = 1;
        break;
    }
    
   
  }	
 
  if( breakloopflag >= 1 )
	  break;

}
 
  if( CheckBlockCollideBoundary( return_xy ) >= 1 )
  {
       Snap2Edge(return_xy);  
       RelativeClickX = RelativeClickY = -1;
       GameGlobals.PlaySound(1);
  }
}
//-------------------------------------------------------------------------------------
public void Draw()
{
  int x, y;
  
for( y = 0; y < SHAPE_HEIGHT; y++ )
 for( x = 0; x < SHAPE_WIDTH; x++ )	 
   if( ButtonGrid[x][y] != null )	
   {   
	 ButtonGrid[x][y].Draw();
	   
	 if( ButtonOverlay[x][y] > 0 )
		 GameGlobals.DrawTileImageOne( GameControl.IMAGE_DIGITS_20x20, (int)(X + x * CELL_WIDTH_PIXELS), (int)(Y + y * CELL_HEIGHT_PIXELS), 
	       GE.LAYER_3, 255, ButtonOverlay[x][y], GameOptions.TilesSelectionIndex , CELL_WIDTH_PIXELS, CELL_HEIGHT_PIXELS);
	 
	 if( GridCollideFlag >= 1 )
		 GameGlobals.DrawTileImageOne( GameControl.IMAGE_DIGITS_20x20, (int)(X + x * CELL_WIDTH_PIXELS), (int)(Y + y * CELL_HEIGHT_PIXELS), 
	       GE.LAYER_3, 255, 10, GameOptions.TilesSelectionIndex , CELL_WIDTH_PIXELS, CELL_HEIGHT_PIXELS);		 
   }
}
//-------------------------------------------------------------------------------------
protected int CheckBlockCollideBoundary( float[][][] return_xy )
{
  int x, y, return_value = 0;
  
  for( y = 0; y < SHAPE_HEIGHT; y++ )
  {	  
    for( x = 0; x < SHAPE_WIDTH; x++ )
       if( ButtonGrid[x][y] != null )   
       {
         if( ButtonGrid[x][y].X < 0 )
         {
            return_xy[0][x][y] = -ButtonGrid[x][y].X;
            return_value = 1;
         }
         else
         if( ButtonGrid[x][y].X + CELL_WIDTH_PIXELS > GRID_WIDTH_CELLS * CELL_WIDTH_PIXELS )
         {
            return_xy[0][x][y] = GRID_WIDTH_CELLS * CELL_WIDTH_PIXELS - (ButtonGrid[x][y].X + CELL_WIDTH_PIXELS);
            return_value = 1;
         }

         
         if( ButtonGrid[x][y].Y < 0 )
         {
            return_xy[1][x][y] = -ButtonGrid[x][y].Y;
            return_value = 1;
         }
         else
         if( ButtonGrid[x][y].Y + CELL_HEIGHT_PIXELS > GRID_HEIGHT_CELLS * CELL_HEIGHT_PIXELS )
         {
            return_xy[1][x][y] = GRID_HEIGHT_CELLS * CELL_HEIGHT_PIXELS - (ButtonGrid[x][y].Y + CELL_HEIGHT_PIXELS); 
            return_value = 1;
         }

       }	
  }
  
  return return_value;
}
//-------------------------------------------------------------------------------------
protected int CheckCollideGrid()
{
  if( ItsDragBoxGrid.CheckCollideDragBox( this ) >= 1 )
  {
	 return 1;
  }
  else
  {
	 return 0;
  }
}
//-------------------------------------------------------------------------------------
protected void Snap2Edge( float[][][] return_xy )
{
	int x, y; float x_shift = 0, y_shift = 0;

  for( y = 0; y < SHAPE_HEIGHT; y++ )
   for( x = 0; x < SHAPE_WIDTH; x++ )
   {
	  if( return_xy[0][x][y] != 0 )
		  x_shift = return_xy[0][x][y];
	  if( return_xy[1][x][y] != 0 )
		  y_shift = return_xy[1][x][y];
   }
  
  X += x_shift; Y += y_shift;
	
  for( y = 0; y < SHAPE_HEIGHT; y++ )
   for( x = 0; x < SHAPE_WIDTH; x++ )
   if( ButtonGrid[x][y] != null )
   {
       ButtonGrid[x][y].X += x_shift;
       ButtonGrid[x][y].Y += y_shift;    
   }	
}
//-------------------------------------------------------------------------------------
protected void RotateRight()
{
 int x, y, x2, y2;
 Button[][] ButtonGridTemp = new Button[SHAPE_WIDTH][SHAPE_HEIGHT];
 int[][] ButtonOverlayTemp = new int[SHAPE_WIDTH][SHAPE_HEIGHT];
                                     
   for( y = 0; y < SHAPE_HEIGHT; y++ )
	 for( x = 0; x < SHAPE_WIDTH; x++ )
	 {
		ButtonGridTemp[x][y] = ButtonGrid[x][y];
        ButtonOverlayTemp[x][y] = ButtonOverlay[x][y];
	 }
   
   x2 = 0;
   for( y = 0; y < SHAPE_HEIGHT; y++ )
   {   
      y2 = SHAPE_HEIGHT - 1;
      for( x = 0; x < SHAPE_WIDTH; x++ )
      {
         ButtonGrid[x][y] = ButtonGridTemp[x2][y2];
         ButtonOverlay[x][y] = ButtonOverlayTemp[x2][y2];
         
         if( ButtonGrid[x][y] != null )
         {
           ButtonGrid[x][y].X = X + x * CELL_WIDTH_PIXELS;
           ButtonGrid[x][y].Y = Y + y * CELL_HEIGHT_PIXELS;    
         }
         y2 = y2 - 1;
      }
         x2 = x2 + 1;
   }	  	 
   
   float[][][] return_xy = new float[2][SHAPE_WIDTH][SHAPE_HEIGHT];
   
   if( CheckBlockCollideBoundary( return_xy ) >= 1 )
   {
     for( y = 0; y < SHAPE_HEIGHT; y++ )
	   for( x = 0; x < SHAPE_WIDTH; x++ )	   
	   {
		 ButtonGrid[x][y] = ButtonGridTemp[x][y];
         ButtonOverlay[x][y] = ButtonOverlayTemp[x][y];
         
         if( ButtonGrid[x][y] != null )
         {
           ButtonGrid[x][y].X = X + x * CELL_WIDTH_PIXELS;
           ButtonGrid[x][y].Y = Y + y * CELL_HEIGHT_PIXELS; 
         }
	   }	  
     
     GameGlobals.PlaySound(1);
   }
   
   GameGlobals.PlaySound(3);
      	
}
//-------------------------------------------------------------------------------------
protected void Snap2Grid()
{
 int x, y; float x_center, y_center;
 
       x_center = X + CELL_WIDTH_PIXELS/2;
       y_center = Y + CELL_HEIGHT_PIXELS/2;
       
       X = (int)(Math.floor(x_center/CELL_WIDTH_PIXELS) * CELL_WIDTH_PIXELS);
       Y = (int)(Math.floor(y_center/CELL_HEIGHT_PIXELS) * CELL_HEIGHT_PIXELS);

       for( y = 0; y < SHAPE_HEIGHT; y++ )
         for( x = 0; x < SHAPE_WIDTH; x++ )
            if( ButtonGrid[x][y] != null )   
            {
              ButtonGrid[x][y].X = X + x * CELL_WIDTH_PIXELS;
              ButtonGrid[x][y].Y = Y + y * CELL_HEIGHT_PIXELS;  
              ButtonGrid[x][y].ClearDupInput();
            }	
}
//-------------------------------------------------------------------------------------
protected void DecreaseLife()
{
  int x, y;
  
for( y = 0; y < SHAPE_HEIGHT; y++ )
 for( x = 0; x < SHAPE_WIDTH; x++ )	 
	if( ButtonOverlay[x][y] > 0 )	
		ButtonOverlay[x][y]--;
}
//-------------------------------------------------------------------------------------
public int GetLife()
{
  int x, y;
  
for( y = 0; y < SHAPE_HEIGHT; y++ )
 for( x = 0; x < SHAPE_WIDTH; x++ )	 
	if( ButtonOverlay[x][y] > 0 )	
		return ButtonOverlay[x][y];
 return 0;
}
//-------------------------------------------------------------------------------------
public void SetLife( int lifecount )
{
  int x, y;
  
for( y = 0; y < SHAPE_HEIGHT; y++ )
 for( x = 0; x < SHAPE_WIDTH; x++ )	 
	if( ButtonGrid[x][y] != null )
	{
       ButtonOverlay[x][y] = lifecount;
       return;
	}
}
//-------------------------------------------------------------------------------------
public void SetPressedFlag( int value )
{
  int i;
  
  for( i = 0; i < DragBoxGrid.MAX_DRAG_BOXES; i++ )
	if( ItsDragBoxGrid.DragBoxList[i] != null )
     ItsDragBoxGrid.DragBoxList[i].PressedFlag = 0;
  PressedFlag = value;
}
//-------------------------------------------------------------------------------------
}
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
