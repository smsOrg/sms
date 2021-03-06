package org.sms.tetris3d.views;

/**
 * Created by hsh on 2016. 11. 29..
 */
import android.content.*;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.widget.*;
import android.view.*;
import android.util.*;
import android.graphics.*;
import android.graphics.drawable.*;

import com.dexafree.materialList.view.MaterialListView;

import org.sms.tetris3d.GameStatus;
import org.sms.tetris3d.controls.RotateControls;
import org.sms.tetris3d.players.DeviceUser;

/**
 * 원안에 파이차트같은 모양을 하고서 각각의 영역을 클릭하면 도형이 회전이 가능하다는 전제하에 해당하는 축을 기준으로 도형을 회전시켜주는 뷰
 *
 * @version 2.2
 *
 * @author 황세현
 *
 */
public class RotateButtonView extends View implements View.OnTouchListener,Runnable{
    /**
     * X축 회전 뷰의 색상
     */
    protected final static int X_AXIS_BUTTON_COLOR = Color.argb(0xff,0x90,0,0);

    /**
     * Y축 회전 뷰의 색상
     */
    protected final static int Y_AXIS_BUTTON_COLOR = Color.argb(0xff,0,0x80,0);

    /**
     * Z축 회전 뷰의 색상
     */
    protected final static int Z_AXIS_BUTTON_COLOR = Color.argb(0xff,0x30,0x30,0xa1);
    /**
     * 영역의 갯수
     */
    public final static int AREA_COUNT = 3;
    /**
     *  360/영역의 갯수
     */
    protected final static float AREA_DEGREE =360.0f/AREA_COUNT;
    /**
     * 한 영역당 할당된 각도
     */
    protected final static float PREFIX_DEGREE = AREA_DEGREE/2;
    /**
     * 아무것도 클릭하지 않거나 알수 없는 곳을 클릭했을떄의 클릭 상태
     */
    protected final static byte UNKNOWN_AXIS_CLICK_INDEX = -1;
    /**
     * X축 뷰를 클릭했을떄의 클릭상태
     */
    protected final static byte X_AXIS_CLICK_INDEX = 2;
    /**
     * Y축 뷰를 클릭했을떄의 클릭상태
     */
    protected final static byte Y_AXIS_CLICK_INDEX = 1;
    /**
     * Z축 뷰를 클릭했을떄의 클릭상태
     */
    protected final static byte Z_AXIS_CLICK_INDEX = 0;

    /**
     * 뷰를 그리기위한 변수
     */
    protected Paint x_pnt=null,y_pnt=null,z_pnt=null,txt_pnt,btnclk_pnt,rm_pnt;
    /**
     * 도형회전을 위한 변수
     */
    protected RotateControls rc = null;
    /**
     *  클릭상태를 받아주는 변수
     */
    protected byte mClickState =UNKNOWN_AXIS_CLICK_INDEX;

    /**
     *  뷰클릭 애니매이션과 도형 회전 뷰를 동시에 처리하기 위한 멀티 쓰레드작업을 하기 위한 변수
     */
    protected final Handler mHandler = new Handler();
    /**
     * 쓰레드 동기화를 위한 변수
     */
    private final Object mSync = new Object();

   // protected final Thread mMultiProcessThread = new Thread(new Runnable(){@Override public void run(){mHandler.post(RotateButtonView.this);}});

    /**
     * 동적생성가능
     * @param ctx
     */
    public RotateButtonView(Context ctx) {
        super(ctx);
        setOnTouchListener(this);
        initPaint();

    }

    /**
     * xml에서 배치할수 있는 생성자
     * @param ctx
     * @param attrs
     */
    public RotateButtonView(Context ctx, AttributeSet attrs) {
         super(ctx,attrs);
        setOnTouchListener(this);
        initPaint();
    }

    /**
     * xml에서 배치할 수 있는 생성자
     * @param ctx
     * @param attrs
     * @param themResId
     */
    public RotateButtonView(Context ctx, AttributeSet attrs,int themResId){
        super(ctx,attrs,themResId);
        setOnTouchListener(this);
        initPaint();
    }

    /**
     * 뷰의 클릭 애니매이션 동작
     */
    @Override
    public void run(){

        synchronized (mSync){
            invalidate();
        }
    }

    /**
     * 페인트 객체 초기화해주는 함수
     */
    private void initPaint(){
        try {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }catch(Exception e){

        }
        x_pnt = new Paint();
        y_pnt = new Paint();
        z_pnt = new Paint();
        txt_pnt = new Paint();
        btnclk_pnt=new Paint();
        rm_pnt=new Paint();
        x_pnt.setAntiAlias(true);
        y_pnt.setAntiAlias(true);
        z_pnt.setAntiAlias(true);
        txt_pnt.setAntiAlias(true);
        btnclk_pnt.setAntiAlias(true);
        rm_pnt.setAntiAlias(true);
        x_pnt.setColor(X_AXIS_BUTTON_COLOR);
        y_pnt.setColor(Y_AXIS_BUTTON_COLOR);
        z_pnt.setColor(Z_AXIS_BUTTON_COLOR);
        rm_pnt.setAlpha(0x0);
        rm_pnt.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        rm_pnt.setColor(Color.WHITE);
        btnclk_pnt.setColor(Color.argb(0x90,0x0,0x0,0x0));

        txt_pnt.setColor(Color.WHITE);
        txt_pnt.setTextSize(45);
    }

    /**
     * 포문을 돌리기 위해서 배열로서 페인트 객체들을 가져옴
     * @return
     */
    Paint[] getPackedPaints(){
        final Paint[] rst = {z_pnt,y_pnt,x_pnt};
        return rst;
    }

    /**
     * 포문을 돌리기 위해서 배열로서 페인트 객체들을 가져옴
     * @return
     */
    String[] getAxisString(){
        return new String[]{"Z","y","X"};
    }

    /**
     * 뷰를 그리는 함수
     * @param canvas
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        final float height =Math.max(getMeasuredHeight(), canvas.getHeight());
        final float width = Math.max(getMeasuredWidth(), canvas.getWidth());
        Paint[] pnt_easy_ary = getPackedPaints(); //{x_pnt,y_pnt,z_pnt};
        canvas.save();
        canvas.rotate(-(90+PREFIX_DEGREE),width/2,height/2);

        RectF area = new RectF();
        if(height>width){
            area.set(0,height/2-width/2,width,height/2+width+2);
        }
        else if(height<width){
            area.set(width/2-height/2,0,width/2+height/2,height);
        }
        else{
            area.set(0,0,width,height);
        }
        for(int i=0;i<pnt_easy_ary.length;i++){
            canvas.drawArc(area,AREA_DEGREE*i,AREA_DEGREE,true ,pnt_easy_ary[i]);
            if(mClickState==i){
                synchronized (mSync) {
                    canvas.drawArc(area, AREA_DEGREE * i, AREA_DEGREE, true, btnclk_pnt);
                }
            }
            //android.util.Log.e("rbv color:",i+"th = "+AREA_DEGREE*(i+1));
        }
        canvas.restore();
        String[] strs = getAxisString();

        for(int i =0;i<strs.length;i++){
            canvas.save();
            canvas.rotate(AREA_DEGREE*i,width/2,height/2);
            String str = strs[i];
            Rect rct = new Rect();
            txt_pnt.getTextBounds(str.toCharArray(),0,str.toCharArray().length,rct);
            canvas.drawText(strs[i],width/2-rct.width()/2,area.height()/5+rct.height()/2,txt_pnt);
            canvas.restore();
        }
        RectF rct = new RectF();
        rct.set(area.width()/2-area.width()*0.05f,area.height()/2-area.height()*0.05f,area.width()/2+area.width()*0.05f,area.height()/2+area.height()*0.05f);
        canvas.drawArc(rct,0,360,true,rm_pnt);
    }

    /**
     * 클릭된 터치좌표로부터 각도를 계산해 반환하는 함수
     * @param r
     * @param relative_xPos
     * @param relative_yPos
     * @return 계산된 각도(라디안 아님)
     */
    protected float getClickedDegree(float r,float relative_xPos,float relative_yPos){
        final float relative_r = (float)Math.sqrt(Math.pow(relative_xPos,2)+ Math.pow(relative_yPos,2));
        float currentDegree =Float.POSITIVE_INFINITY;
        if(r>10){

            if(relative_xPos!=0){
                currentDegree= (float)
                        Math.atan(relative_yPos/relative_xPos);

                if(relative_xPos>0&&relative_yPos>=0){//dai 1
                    currentDegree=(float)Math.PI/2-currentDegree;

                    // android.util.Log.e("pos log: ","dai 1 area");

                }
                else if(relative_xPos>0&&relative_yPos<0){//dai 4
                    currentDegree=(float)Math.PI/2+Math.abs(currentDegree);
                    //android.util.Log.e("pos log: ","dai 4 area");
                }
                else if(relative_xPos<0&&relative_yPos<0){//dai 3
                    currentDegree=(float)Math.PI+((float)Math.PI/2-currentDegree);
                    //android.util.Log.e("pos log: ","dai 3 area");
                }
                else{ //dai 2
                    currentDegree=(float)Math.PI*3/2+Math.abs(currentDegree);
                    //android.util.Log.e("pos log: ","dai 2 area");
                }

                currentDegree=(float)Math.toDegrees(currentDegree);
                //android.util.Log.e("rbv original degree: ",currentDegree+" and pos: "+String.format("%f , %f",relative_xPos,relative_yPos));

            }
            else{
                currentDegree = (relative_yPos>=0)? 0:180;
            }
            if(currentDegree<0){
                currentDegree = (360*3-currentDegree)%360;
            }

        }
            return currentDegree;
    }

    /**
     *
     */
    private float touch_x=0,touch_y = 0,currentDegree=0,move_x=0,move_y=0,moveDegree=0;
    /**
     * 마지막으로 움직인 좌표가 유효한 영역에 존재하는지 체크할 때 사용하는 함수
     */
    private final Rect tmp_area = new Rect();

    /**
     * 입력된 각도로부터 AREA_COUNT분면정보를 가져오는 함수
     * @param deg
     * @return
     */

    public int getQuadrant(float deg){
        if(deg>=360){
            deg-=360;
        }
        if (PREFIX_DEGREE <= deg && deg < PREFIX_DEGREE + AREA_DEGREE) {
            //mMultiProcessThread.start();
            return Y_AXIS_CLICK_INDEX;
        } else if (PREFIX_DEGREE + AREA_DEGREE <= deg && deg < PREFIX_DEGREE + AREA_DEGREE * 2) {
           return X_AXIS_CLICK_INDEX;
        } else if (!Float.isInfinite(deg)) {
            return Z_AXIS_CLICK_INDEX;
        }
        return -1;
    }

    /**
     * 좌표가 유효한 영역에 존재하는 지판단
     * @param ori_deg
     * @param changed_deg
     * @return
     */
    protected boolean determineValidDeltaDegree(float ori_deg,float changed_deg){
        final int q = getQuadrant(ori_deg);
        final int p = getQuadrant(changed_deg);
        android.util.Log.e("degree log: ",String.format("%f %f",ori_deg,changed_deg));
        android.util.Log.e("quadrand log: ",String.format("%d %d",q,p));
        return q!=-1&&q==p;
    }
    //private boolean nowOutSide=false;

    /**
     * 뷰가 클릭했을때 작동하는 함수
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                tmp_area.set(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                //nowOutSide=false;
                touch_x=move_x = event.getX();
                touch_y=move_y = event.getY();
                final float height = Math.max(v.getMeasuredHeight(), v.getHeight());
                final float width = Math.max(v.getMeasuredWidth(), v.getWidth());
                final float r = Math.min(height,width);
                //android.util.Log.e("touch x and y: ",String.format("x= %f y= %f",touch_x,touch_y));
                float relative_xPos = width/2- touch_x;
                float relative_yPos = height/2-touch_y;
                relative_xPos *=-1;
                 currentDegree =moveDegree= getClickedDegree(r,relative_xPos,relative_yPos);
                //android.util.Log.e("rbv degree: ",currentDegree+"");

                if(PREFIX_DEGREE<=currentDegree&&currentDegree<PREFIX_DEGREE+AREA_DEGREE){

                    mClickState=Y_AXIS_CLICK_INDEX;
                    //rc.rotateY(du);
                }
                else if(PREFIX_DEGREE+AREA_DEGREE<=currentDegree&&currentDegree<PREFIX_DEGREE+AREA_DEGREE*2){
                    mClickState=X_AXIS_CLICK_INDEX;
                    //rc.rotateX(du);
                }
                else if(!Float.isInfinite(currentDegree)){
                    mClickState=Z_AXIS_CLICK_INDEX;
                    //rc.rotateZ(du);
                }
                else{
                    mClickState=UNKNOWN_AXIS_CLICK_INDEX;
                }
                synchronized (mSync) {
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                move_x = event.getX();
                move_y = event.getY();
                final float height = Math.max(v.getMeasuredHeight(), v.getHeight());
                final float width = Math.max(v.getMeasuredWidth(), v.getWidth());
                final float r = Math.min(height,width);
                float relative_xPos = width/2- move_x;
                float relative_yPos = height/2-move_y;
                relative_xPos *=-1;
                moveDegree= getClickedDegree(r,relative_xPos,relative_yPos);
                break;
            }
            case MotionEvent.ACTION_UP:{
                mClickState = UNKNOWN_AXIS_CLICK_INDEX;
                boolean tm;
                if((tm=determineValidDeltaDegree(currentDegree,moveDegree))&&tmp_area.contains((int)(v.getLeft() + move_x), (int)(v.getTop() + move_y))) {

                    if (rc == null) {
                        rc = new RotateControls();
                    }
                    final DeviceUser du = GameStatus.getDeviceUser();

                    if (PREFIX_DEGREE <= moveDegree && moveDegree < PREFIX_DEGREE + AREA_DEGREE) {
                        //mMultiProcessThread.start();
                        mHandler.postDelayed(this, 4);
                        rc.rotateY(du);
                    } else if (PREFIX_DEGREE + AREA_DEGREE <= moveDegree && moveDegree < PREFIX_DEGREE + AREA_DEGREE * 2) {
                        // mMultiProcessThread.start();
                        mHandler.postDelayed(this, 4);
                        rc.rotateX(du);
                    } else if (!Float.isInfinite(moveDegree)) {
                        //mMultiProcessThread.start();
                        mHandler.postDelayed(this, 4);
                        rc.rotateZ(du);
                    }
                }
                else{
                  invalidate();
                }
                android.util.Log.e("rbv boolean log: ",Boolean.toString(tm));
                break;
            }
            default:break;
        }
        return true;
    }

}
