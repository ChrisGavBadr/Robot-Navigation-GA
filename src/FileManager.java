
import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Scanner;

public class FileManager {
	
	private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
	private Obstacle[] obstaclesArr;
	private Scanner input;
	
	public FileManager(String fileName) {
		try {
			this.input = new Scanner(new File(fileName));
			setupFromFile();
		} catch (FileNotFoundException e) {
			System.out.println("Error managing file. File cannot be found.");
			e.printStackTrace();
		}
	}
	
	private void setupFromFile() {
		while(input.hasNextLine()) {
			Scanner line = new Scanner(input.nextLine());
			int pointCount = line.nextInt();
			int[] x = new int[pointCount];
			int[] y = new int[pointCount];
			
			for(int i = 0; i < pointCount; i++) {
				x[i] = line.nextInt();
				y[i] = line.nextInt();
			}
			
			obstacles.add(new Obstacle(x, y, pointCount));
			line.close();
		}
		
		obstaclesArr = new Obstacle[obstacles.size()];
		
		for(int i = 0; i < obstacles.size(); i++)
			obstaclesArr[i] = obstacles.get(i);
	}
	
	public Obstacle[] getObstacles() {
		return obstaclesArr;
	}
}