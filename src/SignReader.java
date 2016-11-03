import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Controller.PolicyFlag;
import com.leapmotion.leap.Finger.Joint;
public class SignReader {
	static Frame d = new Frame();
	static ArrayList<Frame> letters = new ArrayList<Frame>();
	static ArrayList<Character> alpha = new ArrayList<Character>();

	static boolean checkHands(Hand h1, Hand h2) {
		for(int l=0; l<h1.fingers().count(); l++){
			// tip
			// distal joint
			// proximal joint
			Vector tip1 = h1.fingers().get(l).jointPosition(Joint.JOINT_TIP);
			Vector pip1 = h1.fingers().get(l).jointPosition(Joint.JOINT_PIP);
			Vector dip1 = h1.fingers().get(l).jointPosition(Joint.JOINT_DIP);
			Vector[] vectors1 = {tip1,dip1,pip1};
			Vector tip2 = h2.fingers().get(l).jointPosition(Joint.JOINT_TIP);
			Vector pip2 = h2.fingers().get(l).jointPosition(Joint.JOINT_PIP);
			Vector dip2 = h2.fingers().get(l).jointPosition(Joint.JOINT_DIP);
			Vector[] vectors2 = {tip2,dip2,pip2};
			
			for(int i=0; i<3; i++) {

				if((vectors1[i].getX() - vectors2[i].getX() > 10)) {
					return false;
				}if((vectors1[i].getY() - vectors2[i].getY() > 10)) {
					return false;
				}if((vectors1[i].getZ() - vectors2[i].getZ() > 10)) {
					return false;
				}
			}

		}
		return true;
	}
	public static int save(ArrayList<Frame> u){
		File dataFile = new File("Alphabet.data");
		try{
			if(!dataFile.exists()){
				dataFile.createNewFile();
			}
		}
		catch(IOException i1){
			return -1;
		}

		try{
			FileOutputStream fileOut = new FileOutputStream(dataFile);
			ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(u);
			objOut.close();
			fileOut.close();
		}
		catch(FileNotFoundException f){
			return -1;
		}
		catch(IOException i2){
			return -1;
		}
		return 1;
	}
	public static ArrayList<Frame> load(){
		ArrayList<Frame> u = null;
		File dataFile = new File("Alphabet.data");
		try{
			FileInputStream fileIn = new FileInputStream(dataFile);
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			u = (ArrayList<Frame>)objIn.readObject();
			objIn.close();
			fileIn.close();
		}
		catch(IOException i){
			return null;
		}
		catch(ClassNotFoundException c){
			return null;
		}
		return u;
	}

	public static void mergeDataFiles() {
		Character s = 'a';
		for(s='a'; s<='z'; s++) {
			alpha.add(s);
			byte[] data;
			try {
				data = Files.readAllBytes(Paths.get(s+".data"));
				if(data != null) {
					Frame f = new Frame();
					f.deserialize(data);
					letters.add(f);
				}
			}catch(Exception e) {
			}
		}
	}
	public static void main(String[] args) {
		
		Controller c = new Controller();
		Listen l = new Listen();
		SignReader.mergeDataFiles();
		c.addListener(l);
		try {
			//d.deserialize(Files.readAllBytes(Paths.get("k.data")));
			//System.out.println(d.hands().frontmost().finger(0).tipPosition());
			System.in.read();
		} catch(Exception e){
		}
	}
}

class Listen extends Listener {
	int count = 0; 
	public void onConnect(Controller controller) {
		System.out.println("Connected");
		controller.enableGesture(Gesture.Type.TYPE_SWIPE);
		controller.setPolicy(PolicyFlag.POLICY_DEFAULT);

	}
	public void onFrame(Controller controller) {
		//System.out.println("Frame available");
		Frame frame = controller.frame();
		Vector i = frame.hands().frontmost().palmPosition();
		if(frame.hands().frontmost().confidence()>0.750) {
			//System.out.println("Confidence ");
			/*
			try {
				Files.write(Paths.get("x.data"), a);
				System.exit(0);
			} catch(Exception e) {
				
			}
			*/
			try {
				boolean check = false;
				for(int f = 0; f<SignReader.letters.size(); f++ ){
					check = check || SignReader.checkHands(SignReader.letters.get(f).hands().frontmost(), frame.hands().frontmost());
					if(check) {
						System.out.println(SignReader.alpha.get(f));
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
}