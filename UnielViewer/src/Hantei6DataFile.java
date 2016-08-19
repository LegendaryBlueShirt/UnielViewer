import java.awt.Rectangle;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class Hantei6DataFile
{
	private static final String identifier = "Hantei6DataFile";
	
	/*
		Hitbox is 4x shorts */
		
	static class FrameAf {
		Gx[] subSprites = new Gx[3];
		boolean mActive;
		int mFrame, mFrameUnk, mDuration, mAff, mBlendMode, mColor;

	}
	
	static class Gx {
		int id, unk, sprNo;
		int mOffsetX,mOffsetY;
        float mRotZ = 0, mRotY = 0, mRotX = 0, mZoomX = 1, mZoomY = 1;
        boolean mHasZoom = false;
	}
	
	static class FrameAs {
		int mFlags, mSpeedHorz, mSpeedVert, mAccelHorz, mAccelVert, mStandState, mCancelFlags, ASMV;
	}
	
	static class FrameAt {
		boolean mActive;
		int mFlags, mProration, mDamage, mRedDamage, mDamageUnk, mCircuitGain;
	}
	
	static class FrameEf {
		int mCommand, mParameter;
		int[] mValues = new int[12];
	}
	
	static class FrameIf {
		int mCommand;
		int[] mValues = new int[12];
	}
	
	static class Frame {
		FrameAf AF;
		FrameAs AS;
		FrameAt AT;
		FrameEf EF;
		FrameIf IF;
		Rectangle[] mHitboxes = new Rectangle[33];
	}
	
	static class Sequence {
		@Override
		public String toString() {
			return mName;
		}
		
		String mName, mMoveName;
		boolean mIsMove, mIsInitialized;
		int mMoveMeter, nSubframes, id;
		
		Frame[] frames;
		Rectangle[] hitboxes;
		FrameAt[] frameAts;
		FrameAs[] frameAss;
		FrameEf[] frameEfs;
		FrameIf[] frameIfs;
	}
	
	HashMap<Integer, Sequence> mSequences = new HashMap<Integer, Sequence>();
	public boolean mInitialized = false;

    byte[] stringBuffer = new byte[32];
	public Hantei6DataFile(ByteBuffer file) throws IOException {
		file.position(0);
		file.get(stringBuffer);
		
		if(!identifier.equals(new String(stringBuffer).trim())){
			throw new IOException("Bad header!");
		}
		file.get(tagBuffer);
		int nSequences;
		if(new String(tagBuffer).equals("_STR")){
			nSequences = readInt(file);
			System.out.println("Number of sequences - "+nSequences);
		} else {
			return;
		}
		
		loadSequences(file, nSequences);
		mInitialized = true;
	}
	
	byte[] tagBuffer = new byte[4];
	void loadSequences(ByteBuffer file, int nSequences) throws IOException {
		while(true) {
			file.get(tagBuffer);

			switch(new String(tagBuffer)) {
				case "PSTR": //Sequence start
					int seq_id = readInt(file);
					Sequence newSequence = new Sequence();
					loadSequence(file, newSequence);
					newSequence.id = seq_id;
					if(newSequence.mIsInitialized)
						mSequences.put(seq_id, newSequence);
					break;
				case "_END": //File end
					System.out.println("Parse successful!");
					return;
				default:
					//Unhandled
			}
		}
	}
	
	static class TempInfo {
		Sequence seq;
		int cur_hitbox =0;
		int cur_AT=0, cur_AS=0, cur_EF=0, cur_IF=0;
	}
	
	boolean initialized;
	int currentFrame, strLen;
	void loadSequence(ByteBuffer file, Sequence sequence) throws IOException {
		currentFrame = 0;
		initialized = false;
		TempInfo info = new TempInfo();
		info.seq = sequence;
		while(true) {
			file.get(tagBuffer);

			switch(new String(tagBuffer)) {
				case "PTCN":
					//Unknown string specification
					strLen = readInt(file);
					file.get(new byte[strLen]); //Skip these bytes
					break;
				case "PSTS":
				case "PLVL":
				case "PFLG":
					break;
				case "PTT2": //Variable length title
					strLen = readInt(file);
					file.get(stringBuffer, 0, ((strLen+3)/4)*4);
					sequence.mName = new String(stringBuffer,0,((strLen+3)/4)*4, "Shift_JIS").trim();
					break;
				case "PTIT": //Fixed length title
					byte[] tit = new byte[32];
					file.get(tit);
					sequence.mName = new String(tit, "Shift_JIS").trim();
					break;
				case "PDST": //Strange Allocation
					break;
				case "PDS2": //Allocation
					int count = readInt(file);
					if(count == 32) {
						sequence.frames = new Frame[readInt(file)];
						sequence.hitboxes = new Rectangle[readInt(file)];
						sequence.frameEfs = new FrameEf[readInt(file)];
						sequence.frameIfs = new FrameIf[readInt(file)];
						sequence.frameAts = new FrameAt[readInt(file)];
						readInt(file);
						sequence.frameAss = new FrameAs[readInt(file)];
						
						//Load everything
						sequence.nSubframes = 0;
						sequence.mIsInitialized = true;
						initialized = true;
					} else {
						file.get(new byte[((count/4)-1)*4]); //Skip these bytes
					}
					break;
				case "FSTR": //Frame
					if(sequence.mIsInitialized) {
						Frame newFrame = new Frame();
						sequence.frames[currentFrame++] = newFrame;
						loadFrame(file, info, newFrame);
						sequence.nSubframes += newFrame.AF.mDuration;
					}
					break;
				case "PEND": //End
					return;
				default:
					//Unhandled
			}
		}
	}
	
	int n;
	public void loadFrame(ByteBuffer file, TempInfo info, Frame frame) throws IOException {
		while(true) {
			n=0;
			file.get(tagBuffer);
			
			switch(new String(tagBuffer)) {
				case "HRAT":
					n+=25;
				case "HRNM":
					n += readInt(file);
					if(n<=32 && info.cur_hitbox < info.seq.hitboxes.length){
						int boxX = readInt(file);
						int boxY = readInt(file);
						int boxWidth = readInt(file)-boxX;
						int boxHeight = readInt(file)-boxY;
						frame.mHitboxes[n] = new Rectangle(boxX,boxY,boxWidth,boxHeight);
						info.seq.hitboxes[info.cur_hitbox++]=frame.mHitboxes[n];
					} else {
						file.get(new byte[32]);
					}
					break;
				case "HRAS": //Referenced boxes
					n+=25;
				case "HRNS":
					n+= readInt(file);
					int source = readInt(file);
					if(n <= 32 && source < info.cur_hitbox) {
						frame.mHitboxes[n] = info.seq.hitboxes[source];
					}
					break;
				case "ATST": //Start Attack Block
					if(info.cur_AT < info.seq.frameAts.length){
						frame.AT = new FrameAt();
						loadFrameAt(file, frame.AT);
						info.seq.frameAts[info.cur_AT++] = frame.AT;
					}
					break;
				case "ASST":
					if(info.cur_AS < info.seq.frameAss.length){
						frame.AS = new FrameAs();
						loadFrameAs(file, frame.AS);
						info.seq.frameAss[info.cur_AS++] = frame.AS;
					}
					break;
				case "ASSM":
					int value = readInt(file);
					if(value < info.cur_AS){
						frame.AS = info.seq.frameAss[value];
					}
					break;
				case "AFST":
					frame.AF = new FrameAf();
					loadFrameAf(file,frame.AF);
					break;
				case "EFST":
					int efn = readInt(file);
					if((efn < 8) && (info.cur_EF < info.seq.frameEfs.length)) {
						frame.EF = new FrameEf();
						loadFrameEf(file, frame.EF);
						info.seq.frameEfs[info.cur_EF++] = frame.EF;
					}
					break;
				case "IFST":
					int ifn = readInt(file);
					if((ifn < 8) && (info.cur_IF < info.seq.frameIfs.length)) {
						frame.IF = new FrameIf();
						loadFrameIf(file, frame.IF);
						info.seq.frameIfs[info.cur_IF++] = frame.IF;
					}
					break;
				case "FSNH":
					readInt(file); ///Number of hitboxes, not very useful
					break;
				case "FEND":
					return;
				default:
					break;
				
			}
		}
	}
	
	public void loadFrameAt(ByteBuffer file, FrameAt at) throws IOException {
		at.mActive = true;
		at.mFlags = 0;
		at.mProration = 0;
		at.mDamage = 0;
		at.mRedDamage = 0;
		at.mCircuitGain = 0;
		
		while(true) {
			n=0;
			file.get(tagBuffer);

			switch(new String(tagBuffer)) {
				case "ATGD": //Guardflags
					at.mFlags = readInt(file);
					break;
				case "ATHS": //Proration
					at.mProration = readInt(file);
					break;
				case "ATVV": //Damage and gain
					at.mRedDamage = readShort(file);
					at.mDamage = readShort(file);
					at.mDamageUnk = readShort(file);
					at.mCircuitGain = readShort(file);
					break;
				case "ATED": //End
					return;
				default:
					break;
			}
		}
	}
	
	public void loadFrameAs(ByteBuffer file, FrameAs as) throws IOException {
		as.mFlags = 0;
		as.ASMV = -1;
		as.mStandState = 0;
		as.mCancelFlags = 0;

		while(true) {
			n=0;
			file.get(tagBuffer);

			switch(new String(tagBuffer)) {
				case "ASV0":
				case "ASVX":
				case "ASMV":
				case "ASS1":
				case "ASS2":
				case "ASCN":
				case "ASCS":
				case "ASCT":
				case "AST0":
					break;
				case "ASED":
					return;
				default:
					break;
			}
		}
	}
	
	public void loadFrameEf(ByteBuffer file, FrameEf ef) throws IOException {
		while(true) {
			n=0;
			file.get(tagBuffer);

			switch(new String(tagBuffer)) {//TODO
				case "EFTP":
				case "EFNO":
				case "EFPR":
					break;
				case "EFED":
					return;
				default:
					break;
			}
		}
	}
	
	public void loadFrameIf(ByteBuffer file, FrameIf frameIf) throws IOException {
		while(true) {
			n=0;
			file.get(tagBuffer);

			switch(new String(tagBuffer)) {//TODO
				case "IFTP":
				case "IFPR":
					break;
				case "IFED":
					return;
				default:
					break;
			}
		}
	}
	
	int currentGx, currentOffx, currentOffy;
	public void loadFrameAf(ByteBuffer file, FrameAf af) throws IOException {
		af.mActive = true;
		af.mFrame = -1;
		af.mFrameUnk = -1;
		af.mDuration = 1;
		af.mAff = -1;
		af.mBlendMode = 0;
		af.mColor = -1;
		currentOffx = 0;
		currentOffy = 0;
		
		while(true) {
			n=0;
			file.get(tagBuffer);
			
			String code = new String(tagBuffer);
			switch(code) {
				case "AFGX":
					Gx subSprite = new Gx();
					subSprite.id = readInt(file);
					subSprite.unk = readInt(file);
					subSprite.sprNo = readInt(file);
					currentGx = subSprite.id;
					af.subSprites[currentGx] = subSprite;
					subSprite.mOffsetX = currentOffx;
					subSprite.mOffsetY = currentOffy;
					break;
				case "AFPA":
				case "AFLP":
				case "AFCT":
					readInt(file); //Unknown to me
					break;
				case "AFGP":
					af.mFrameUnk = readInt(file);
					af.mFrame = readInt(file);
					break;
				case "AFOF":
					currentOffx = readInt(file);
					currentOffy = readInt(file);
					af.subSprites[currentGx].mOffsetX = currentOffx;
					af.subSprites[currentGx].mOffsetY = currentOffy;
					break;
				case "AFAL":
					af.mBlendMode = readInt(file);//Used to be one byte.
					af.mColor = (af.mColor&0xFFFFFF) | (readInt(file)); //This is the alpha component, also used to be one byte <<24
					break;
				case "AFRG":
					int color = (readInt(file)<<16)|(readInt(file)<<8)|readInt(file); //Used to be three bytes
					af.mColor = (af.mColor&0xFF000000) | color;
					break;
				case "AFAZ":
					af.subSprites[currentGx].mRotZ = readFloat(file);
					break;
				case "AFAY":
					af.subSprites[currentGx].mRotY = readFloat(file);
					break;
				case "AFAX":
					af.subSprites[currentGx].mRotX = readFloat(file);
					break;
				case "AFZM":
					af.subSprites[currentGx].mHasZoom = true;
					af.subSprites[currentGx].mZoomX = readFloat(file);
					af.subSprites[currentGx].mZoomY = readFloat(file);
					break;
				case "AFED":
					return;
				case "AFPL":
					readInt(file); //Palette shenanigans?
					break;
				case "AFHK":
				case "AFPR":
				case "AFID":
                case "AFRT": //Unknown
					readInt(file);
					break;
				default: //We need to check for 3 letter codes
					byte t = tagBuffer[3];
					if(code.startsWith("AFD")) {
						if((t >= '0') && (t <= '9')) {
							af.mDuration = t - '0';
						} else if(t == 'L') {
							af.mDuration = readInt(file);
						}
					} else if(code.startsWith("AFY")){
						if((t >= '0') && (t <= '9')) {
							int v = t - '0';
							if(v < 4)
								v+=10;
							af.subSprites[currentGx].mOffsetY = v;
						} else if(t== 'X'){
							af.subSprites[currentGx].mOffsetY = 10;
						}
					} else if(code.startsWith("AFF")){
						if((t >= '0') && (t <= '9')) {
							af.mAff = t - '0';
						} else if(t== 'X'){
							af.mAff = readInt(file);
						} else {
							readInt(file);//Unknown
						}
					} else if(code.startsWith("AFJ")){
						readInt(file); //Unknown
					} else {
						System.out.println("Unknown tag "+code+" at "+(file.position()-4));
						if(!code.startsWith("AF"))
							System.exit(0);
					}
					break;
			}
		}
	}				
	
	byte[] buffer = new byte[4];
	public int readInt(ByteBuffer file) throws IOException {
		file.get(buffer);
		return byteToInt(buffer);
	}
	
	public float readFloat(ByteBuffer file) throws IOException {
		file.get(buffer);
		return byteToFloat(buffer);
	}
	
	public short readShort(ByteBuffer file) throws IOException{
		file.get(buffer,0,2);
		return byteToShort(buffer);
	}
	
	public float byteToFloat(byte[]  buffer) {
		return Float.intBitsToFloat(byteToInt(buffer));
		//return (buffer[0]&0xff) |((buffer[1]&0xff)<<8) |((buffer[2]&0xff)<<16) |((buffer[3]&0xff)<<24);
	}
	
	public int byteToInt(byte[]  buffer) {
		return (buffer[0]&0xff) |((buffer[1]&0xff)<<8) |((buffer[2]&0xff)<<16) |((buffer[3]&0xff)<<24);
	}
	
	public short byteToShort(byte[]  buffer) {
		return (short)((buffer[0]&0xff) |((buffer[1]&0xff)<<8));
	}
}