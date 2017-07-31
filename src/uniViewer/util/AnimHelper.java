package uniViewer.util;
import uniViewer.model.Hantei6DataFile;

public class AnimHelper {
	private AnimHelper(){}
	
	public static int getSequenceDurationTotal(Hantei6DataFile.Sequence sequence) {
		int total = 0;
		for(Hantei6DataFile.Frame frame: sequence.frames) {
			total += frame.AF.mDuration;
		}
		return total;
	}
	
	public static int getTimeForFrame(Hantei6DataFile.Sequence sequence, int frame) {
		int time = 0;
		for(int n = 0;n < frame;n++) {
			time += sequence.frames[n].AF.mDuration;
		}
		return time;
	}
	
	public static int getFrameForTime(Hantei6DataFile.Sequence sequence, int time) {
		int currentTime = time%getSequenceDurationTotal(sequence);
		int frameNum = -1;
		for(Hantei6DataFile.Frame frame: sequence.frames) {
			frameNum++;
			currentTime -= frame.AF.mDuration;
			if(currentTime < 0)
				break;
		}
		return frameNum;
	}
}
