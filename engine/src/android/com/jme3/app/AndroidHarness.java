package com.jme3.app;

import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.jme3.app.Application;
import com.jme3.input.TouchInput;
import com.jme3.input.android.AndroidInput;
import com.jme3.input.controls.TouchListener;
import com.jme3.input.controls.TouchTrigger;
import com.jme3.input.event.TouchEvent;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jme3.system.android.OGLESContext;
import com.jme3.system.android.AndroidConfigChooser.ConfigType;


/**
 * <code>AndroidHarness</code> wraps a jme application object and runs it on Android
 * @author Kirill
 * @author larynx
 */
public class AndroidHarness extends Activity implements TouchListener, DialogInterface.OnClickListener
{
    protected final static Logger logger = Logger.getLogger(AndroidHarness.class.getName());
    
    /**
     * The application class to start
     */
    protected String appClass = "jme3test.android.Test";
    
    /**
     * The jme3 application object
     */
    protected Application app = null;
    
    /**
     * ConfigType.FASTEST is RGB565, GLSurfaceView default
     * ConfigType.BEST is RGBA8888 or better if supported by the hardware
     */
    protected ConfigType eglConfigType = ConfigType.FASTEST;
    
    /**
     * If true all valid and not valid egl configs are logged
     */
    protected boolean eglConfigVerboseLogging = false;

    /**
     * Title of the exit dialog, default is "Do you want to exit?"
     */
    protected String exitDialogTitle = "Do you want to exit?";
    /**
     * Message of the exit dialog, default is "Use your home key to bring this app into the background or exit to terminate it."
     */
    protected String exitDialogMessage = "Use your home key to bring this app into the background or exit to terminate it.";

          
    protected OGLESContext ctx;
    protected GLSurfaceView view;
    
    final private String ESCAPE_EVENT = "TouchEscape"; 

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        JmeSystem.setResources(getResources());
        JmeSystem.setActivity(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AppSettings settings = new AppSettings(true);
        AndroidInput input = new AndroidInput(this);
                
        // Create application instance
        try{
            @SuppressWarnings("unchecked")
            Class<? extends Application> clazz = (Class<? extends Application>) Class.forName(appClass);
            app = clazz.newInstance();
        }catch (Exception ex){
            handleError("Class " + appClass + " init failed", ex);
        }

        app.setSettings(settings);
        app.start();    
        ctx = (OGLESContext) app.getContext();
        view = ctx.createView(input, eglConfigType, eglConfigVerboseLogging);
   		setContentView(view);       		
    }


    @Override
    protected void onRestart(){
        super.onRestart(); 
        app.restart();
        logger.info("onRestart");
    }
    

    @Override
    protected void onStart(){
        super.onStart();
        logger.info("onStart");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        view.onResume();
        logger.info("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        view.onPause();
        logger.info("onPause");
    }
    
    @Override
    protected void onStop(){
        super.onStop();
        logger.info("onStop");
    }

    @Override
    protected void onDestroy(){
        app.stop(true);
        super.onDestroy();                
        logger.info("onDestroy");
    }

    public Application getJmeApplication()
    {
        return app;
    }
    
    /**
     * Called when an error has occured. This is typically
     * invoked when an uncought exception is thrown in the render thread.
     * @param errorMsg The error message, if any, or null.
     * @param t Throwable object, or null.
     */
    public void handleError(final String errorMsg, final Throwable t)
    {
        
        String s = "";
        if (t != null && t.getStackTrace() != null)
        {
            for (StackTraceElement ste: t.getStackTrace())
            {
                s +=  ste.getClassName() + "." + ste.getMethodName() + "(" + + ste.getLineNumber() + ") ";
            }
        }                
        
        final String sTrace = s;
        
        logger.severe(t != null ? t.toString() : "OpenGL Exception");
        logger.severe((errorMsg != null ? errorMsg + ": " : "") + sTrace);
        
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() 
            {                                
                AlertDialog dialog = new AlertDialog.Builder(AndroidHarness.this)
               // .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(t != null ? (t.getMessage() != null ? (t.getMessage() + ": " + t.getClass().getName()) : t.getClass().getName()) : "OpenGL Exception")
                .setPositiveButton("Kill", AndroidHarness.this)
                .setMessage((errorMsg != null ? errorMsg + ": " : "") + sTrace)
                .create();    
                dialog.show();                
            }
        });
        
    }
    
    /**
     * Called by the android alert dialog, terminate the activity and OpenGL rendering
     * @param dialog
     * @param whichButton
     */
    public void onClick(DialogInterface dialog, int whichButton) 
    {        
        if (whichButton != -2)
        {
            app.stop(true);
            this.finish();
        }
    }
    
    /**
     * Gets called by the InputManager on all touch/drag/scale events
     */    
    @Override    
    public void onTouch(String name, TouchEvent evt, float tpf)  
    {
        if (name.equals(ESCAPE_EVENT))
        {
            switch(evt.getType())
            {                
                case KEY_UP:
                    this.runOnUiThread(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {                                                
                            AlertDialog dialog = new AlertDialog.Builder(AndroidHarness.this)
                           // .setIcon(R.drawable.alert_dialog_icon)
                            .setTitle(exitDialogTitle)
                            .setPositiveButton("Yes", AndroidHarness.this)
                            .setNegativeButton("No", AndroidHarness.this)
                            .setMessage(exitDialogMessage)
                            .create();    
                            dialog.show();                
                        }
                    });
                            
                            
                    break;
                    
               default:
                   break;
            }
        }
                        
    }    
    
}
