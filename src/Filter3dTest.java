import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.IOException;
import javax.sound.sampled.*;
import javax.swing.ImageIcon;

/*
 * The Filter3dTest class demonstrates the Filter3d functionality. A fly buzzes around the listener, and the closer the fly is, the louder it's heard.
 * @see Filter3d
 * @see SimpleSoundPlayer
 */

public class Filter3dTest extends GameCore {
	
	public static void main(String[] args) {
		new Filter3dTest().run();
	}
	
	private Sprite fly;
	private Sprite listener;
	private InputManager inputManager;
	private GameAction exit;
	
	private SimpleSoundPlayer bzzSound;
	private InputStream bzzSoundStream;
	
	public void init() {
		super.init();
		
		//set up input manager
		exit = new GameAction("exit", GameAction.DETECT_INITIAL_PRESS_ONLY);
		inputManager = new InputManager(screen.getFullScreenWindow());
		inputManager.mapToKey(exit,  KeyEvent.VK_ESCAPE);
		inputManager.setCursor(InputManager.INVISIBLE_CURSOR);
		
		createSprites();
		
		//load the sound
		bzzSound = new SimpleSoundPlayer("sound/fly-bzz.wav");
		
		//create the 3D filter
		Filter3d filter = new Filter3d(fly, listener, screen.getHeight());
		
		//create the filtered sound stream
		bzzSoundStream = new FilteredSoundStream(new LoopingByteInputStream(bzzSound.getSamples()), filter);
		
		//play the sound in a separate thread
		new Thread() {
			public void run() {
				bzzSound.play(bzzSoundStream);
			}
		}.start();
	}
	
	/*
	 * Loads images and creates sprites
	 */
	private void createSprites() {
		//load images
		Image fly1 = loadImage("images/fly1.png");
		Image fly2 = loadImage("images/fly2.png");
		Image fly3 = loadImage("images/fly3.png");
		Image ear = loadImage("images/ear.png");
		
		//create "fly" sprite
		Animation anim = new Animation();
		anim.addFrame(fly1,  50);
		anim.addFrame(fly2,  50);
		anim.addFrame(fly3, 50);
		anim.addFrame(fly2, 50);
		
		fly = new Sprite(anim);
		
		//create the listener sprite
		anim = new Animation();
		anim.addFrame(ear,  0);
		listener = new Sprite(anim);
		listener.setX((screen.getWidth() - listener.getWidth()) / 3);
		listener.setY((screen.getHeight() - listener.getHeight())/ 3);
	}
	
	private Image loadImage(String fileName){
		return new ImageIcon(fileName).getImage();
	}
	
	public void update(long elapsedTime) {
		if(exit.isPressed()) {
			//stop();
			laxilyExit();
		}else {
			listener.update(elapsedTime);
			fly.update(elapsedTime);
			fly.setX(inputManager.getMouseX());
			fly.setY(inputManager.getMouseY());
		}
	}
	
	public void stop() {
		super.stop();
		//stop the bzz sound
		try {
			bzzSoundStream.close();
		}
		catch(IOException ex) { }
	}
	
	public void draw(Graphics2D g) {
		//draw background
		g.setColor(new Color(0x33cc33));
		g.fillRect(0,  0,  screen.getWidth(),  screen.getHeight());
		
		//draw listener
		g.drawImage(listener.getImage(), Math.round(listener.getX()), Math.round(listener.getY()), null);
		
		//draw fly
		g.drawImage(fly.getImage(), Math.round(fly.getX()), Math.round(listener.getY()), null);
	}
	
	public void laxilyExit() {
		Thread thread = new Thread() {
			public void run() {
				//first, wait for the VM exit on its own.
				try {
					Thread.sleep(2000);
				}
				catch(InterruptedException ex) { }
				//System is still running, so force an exit
				System.exit(0);
			}
		};
		thread.setDaemon(true);
		thread.start();
	}
}
