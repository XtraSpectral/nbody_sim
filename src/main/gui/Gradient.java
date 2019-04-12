package main.gui;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Gradient {
	
	public static final double COLOR_SCALE = 50.0;
	public static final int GRAD_SCALE = (int) COLOR_SCALE*7; // corresponds to number of color segments in gradients
	public static LinkedList<Color> colorList = new LinkedList<Color>();
	public static final int minRGB = 0; // 0 - full saturation, 255 - all white
	public static final double alpha = .8; // 0 - clear coloring, 255 - solid coloring
	private static int next = 0;
	public static Map<String, Color> allColors = new HashMap<String, Color>();
	private static int spectraIndex = 0;
	private static String spectra;
	private static String wavelength;
	
	private static int dopplerShift; // from -50 to 50
	public static Stack<Color> dopplerOverflow = new Stack<Color>();

	
	
	public static Color getJavaColor(int index) {
		String s = (String) allColors.keySet().toArray()[index];
		return allColors.get(s);
		
		
	}
	
	public static void dopplerShift(int shift) {	
		
		int diff = Math.abs(shift - dopplerShift);
		for (int i=0; i<diff; i++) {
			if (shift < dopplerShift) { // if redshifting (chosen arbitrarily to be represented by negative integers)
				Color minColor = Color.rgb(minRGB, minRGB, minRGB);
				if (colorList.get(0).equals(minColor)) {
					colorList.addFirst(minColor);
				} else {
					int newR = (int) (colorList.get(0).getRed()*255) - 1;
					int newG = (int) (colorList.get(0).getGreen()*255) - 1;
					int newB = (int) (colorList.get(0).getBlue()*255) - 1;
					newR = newR < 0 ? 0 : newR;
					newG = newG < 0 ? 0 : newG;
					newB = newB < 0 ? 0 : newB;
					Color newColor = Color.rgb(newR, newG, newB);
					colorList.addFirst(newColor);
				}
				dopplerOverflow.push(colorList.get(GRAD_SCALE));
				colorList.removeLast();
			} else if (shift > dopplerShift) {
				if (!dopplerOverflow.isEmpty()) {
					colorList.removeFirst();
					colorList.addLast(dopplerOverflow.pop());
				}
			}
		}
		dopplerShift = shift;
	}
	
	public static String getSpectra() {
		// blueshift can happen at >0, <0, but decided to show no
		// doppler shift labels within a slightly larger range around 0
		if (dopplerShift > 20) {
			return "Blueshift " + spectra;
		} else if (dopplerShift < -20) {
			return "Redshift " + spectra;
		}
		return spectra;
	}
	
	public static String getWavelength() {
		return wavelength;
	}
	
	public static void generateGradient() {
		loadJavaFXColors();
		alternativeGradientC();
	}
	
//	public static void changePalette(Spectra spectrum) {
//		List<Color> palette = new ArrayList<Color>();
//		List<Integer> weights = new ArrayList<Integer>();
//		Color dark_grey = Color.rgb(minRGB, minRGB, minRGB, alpha);
//		Color purple = Color.rgb(255, minRGB, 255, alpha);
//		Color blue = Color.rgb(minRGB, minRGB, 255, alpha);
//		Color green = Color.rgb(minRGB, 255, minRGB, alpha);
//		Color yellow = Color.rgb(minRGB, 255, minRGB, alpha);
//		Color orange = Color.rgb(minRGB, 255, minRGB, alpha);
//		Color red = Color.rgb(minRGB, 255, minRGB, alpha);
//		spectra = spectrum.title;
//		wavelength = spectrum.about;
//		switch (spectrum.id) {
//			case "midinfrared":
//				palette.add(dark_grey); // lower-outlier color
//				palette.add(dark_grey, 13);
//				palette.add(purple, 12);
//				palette.add(blue, 13);
//				palette.add(green, 12);
//				palette.add(yellow, 13);
//				palette.add(orange, 12);
//				palette.add(red, 13); // upper-outlier color
//				break;
//			case "longuv":
//				palette.add(dark_grey, 12); // lower-outlier color
//				palette.add(dark_grey, 13);
//				palette.add(grey, 12);
//				palette.add(dark_purple, 13);
//				palette.add(indigo, 12);
//				palette.add(blue, 13);
//				palette.add(cyan, 12);
//				palette.add(green, 13); // upper-outlier color
//			default:
//		}
//		createSpectrum(palette);
//	}

	public static void alternativeGradientB() {
		spectra = "Long Wave UVA";
		wavelength = "300-550 Nm wavelength";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			int hex = (int) (diff * 255); // hex code on scale of 0-255
			int ihex = 255 - hex; // inverted hex code, for traversing 255 -> 0
			int hhex = (int) hex/2; // half hex value
			int qhex = (int) hex/4; //quarter hex value
			
			if (hex < minRGB) {hex = minRGB;}
			if (ihex < minRGB) {ihex = minRGB;}
			if (hhex < minRGB) {hhex = minRGB;}
			if (qhex < minRGB) {qhex = minRGB;}

			if (counter <= 1) {
				// dark grey (0,0,0)
				c = Color.rgb(minRGB, minRGB, minRGB, alpha);
			} else if (counter > 1 && counter <= 2) {
				// dark grey to grey (0,0,0) -> (127,127,127)
				c = Color.rgb(hhex, hhex, hhex, alpha);
			} else if (counter > 2 && counter <= 3) {
				// grey to dark purple (127,127,127) -> (127,0,127)
				int b = 127-hhex; if (b<minRGB) {b = minRGB;}
				c = Color.rgb(127, b, 127, alpha);			
			} else if (counter > 3 && counter <= 4) {
				// dark purple to indigo (127,0,127) -> (127,0,255)
				c = Color.rgb(127, minRGB, 127+hhex, alpha);			
			} else if (counter > 4 && counter <= 5) {
				// indigo to blue (127,0,255) -> (0,0,255)
				int b = 127-hhex; if (b<minRGB) {b = minRGB;}
				c = Color.rgb(b, minRGB, 255, alpha);	
			} else if (counter > 5 && counter <= 6) {
				// blue to cyan (0,0,255) -> (0,255,255)
				c = Color.rgb(minRGB, hex, 255, alpha);		
			} else if (counter > 6 && counter <= 7) {
				// cyan to green (0,255,255) -> (0,255,0)
				c = Color.rgb(minRGB, 255, ihex, alpha);			
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				// green
				dopplerOverflow.add(Color.rgb(minRGB, 255, minRGB, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientA() {
		spectra = "Mid/Thermal Infrared";
		wavelength = "3-8 Mm wavelength";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			int hex = (int) (diff * 255);
			if (counter <= 1) {
				// dark grey
				c = Color.rgb(minRGB, minRGB, minRGB, alpha);
			} else if (counter > 1 && counter <= 2) {
				// dark grey to purple
				if (hex==0) hex = 1;
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(hex, minRGB, hex, alpha);
			} else if (counter > 2 && counter <= 3) {
				// purple to blue
				int b = 255 - hex;
				if (b<minRGB) {b=minRGB;}
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(b, minRGB, 255, alpha);			
			} else if (counter > 3 && counter <= 4) {
				// blue to green
				int b = 255 - hex;
				if (b<minRGB) {b=minRGB;}
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(minRGB, hex, b, alpha);				
			} else if (counter > 4 && counter <= 5) {
				// green to yellow
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(hex, 255, minRGB, alpha);		
			} else if (counter > 5 && counter <= 6) {
				// yellow to orange
				int b = 255 - (int) (hex/2);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(255, b, minRGB, alpha);	
			} else if (counter > 6 && counter <= 7) {
				// orange to red
				int b = 127 - (int) (hex/2);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(255, b, minRGB, alpha);	
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				// red
				dopplerOverflow.add(Color.rgb(255, minRGB, minRGB, alpha));	
			}
			colorList.add(c);
		}
	}

	
	/**
	 * Balanced palette that eliminates eccentric colors
	 */
	public static void alternativeGradientC() {
		spectra = "Long-Infrared";
		wavelength = "8-14 Mm wavelength";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			int hex = (int) (diff * 255);
			if (counter <= 1) {
				// dark grey
				c = Color.rgb(minRGB, minRGB, minRGB, alpha);
			} else if (counter > 1 && counter <= 2) {
				// dark grey to blue
				if (hex==0) hex = 1;
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(minRGB, minRGB, hex, alpha);
			} else if (counter > 2 && counter <= 3) {
				// blue to purple
				if (hex==0) hex = 1;
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(hex, minRGB, 255, alpha);			
			} else if (counter > 3 && counter <= 4) {
				// purple to red
				int b = 255 - hex;
				if (b<minRGB) {b=minRGB;}
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(255, minRGB, b, alpha);				
			} else if (counter > 4 && counter <= 5) {
				// red to orange
				int b = (int) (hex/2);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(255, b, minRGB, alpha);		
			} else if (counter > 5 && counter <= 6) {
				// orange to bright orange
				int b = (int) 127 + (hex/4);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(255, b, minRGB, alpha);	
			} else if (counter > 6 && counter <= 7) {
				// bright orange to yellow
				c = Color.rgb(255, ((int) (hex/4) + 190), minRGB, alpha);	
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				dopplerOverflow.add(Color.rgb(255, 255, minRGB, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientD() {
		spectra = "Near Infrared";
		wavelength = "0.5-3 Mm wavelength";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			int hex = (int) (diff * 255);
			if (counter <= 1) {
				// dark grey
				c = Color.rgb(minRGB, minRGB, minRGB, alpha);
			} else if (counter > 1 && counter <= 2) {
				// dark grey to dark red
				int b = (int) (hex/2);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(b, minRGB, minRGB, alpha);	
			} else if (counter > 2 && counter <= 3) {
				// dark red to red
				int b = (int) 127 + (hex/2);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(b, minRGB, minRGB, alpha);	
			} else if (counter > 3 && counter <= 4) {
				// red to orange
				int b = (int) (hex/2);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(255, b, minRGB, alpha);					
			} else if (counter > 4 && counter <= 5) {
				// orange to bright orange
				int b = (int) 127 + (hex/4);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(255, b, minRGB, alpha);		
			} else if (counter > 5 && counter <= 6) {
				// bright orange to yellow
				c = Color.rgb(255, ((int) (hex/4) + 190), minRGB, alpha);		
			} else if (counter > 6 && counter <= 7) {
				// yellow to white
				c = Color.rgb(255, 255, hex, alpha);	
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				dopplerOverflow.add(Color.rgb(255, 255, 255, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientE() {
		spectra = "UV Filter";
		wavelength = "Blue wavelengths only";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			int hex = (int) (diff * 255);
			if (counter <= 1) {
				// dark grey
				c = Color.rgb(minRGB, minRGB, minRGB, alpha);
			} else if (counter > 1 && counter <= 2) {
				// dark grey to dark mid grey
				int b = (int) (hex/4);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(b, b, b, alpha);	
			} else if (counter > 2 && counter <= 3) {
				// dark mid grey to mid grey
				int b = (int) (hex/4);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(63+b,63+b,63+b, alpha);	
			} else if (counter > 3 && counter <= 4) {
				// mid grey to light grey
				int b = (int) (hex/4);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(127+b,127+b,127+b, alpha);					
			} else if (counter > 4 && counter <= 5) {
				// light grey to white
				int b = (int) (hex/4);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(190+b,190+b,190+b, alpha);			
			} else if (counter > 5 && counter <= 6) {
				// white
				c = Color.rgb(255,255,255, alpha);	
			} else if (counter > 6 && counter <= 7) {
				// white to blue
				hex = 255-hex;
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(hex, hex, 255, alpha);		
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				dopplerOverflow.add(Color.rgb(minRGB, minRGB, 255, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientF() {
		spectra = "Void and Singularity";
		wavelength = "Outliers only";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			int hex = (int) (diff * 255);
			if (counter <= 1) {
				// light mid green to mid green
				int b = 192 - (int) (hex/4);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(minRGB, b, minRGB, alpha);	
			} else if (counter > 1 && counter <= 2) {
				// mid green to dark green
				int b = 127 - (int) (hex/4);
				int darken = b < minRGB ? b : minRGB;
				c = Color.rgb(darken,b,darken, alpha);	
			} else if (counter > 2 && counter <= 3) {
				// dark green to void black
				int b = 63 - (int) (hex/4);
				int darken = b < minRGB ? b : minRGB;
				c = Color.rgb(darken,b,darken, alpha);				
			} else if (counter > 3 && counter <= 4) {
				// void black
				c = Color.rgb(0, 0, 0, alpha);		
			} else if (counter > 4 && counter <= 5) {
				// void black
				c = Color.rgb(0, 0, 0, alpha);	
			} else if (counter > 5 && counter <= 6) {
				// void black
				c = Color.rgb(0, 0, 0, alpha);		
			} else if (counter > 6 && counter <= 7) {
				// black to white, very delayed
				if (hex<127+63) {hex = 0;
				} else {hex = (hex-127-63)*4;}
				if (hex>255) {hex=255;}
				c = Color.rgb(hex, hex, hex, alpha);		
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				dopplerOverflow.add(Color.rgb(255, 255, 255, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientG() {
		spectra = "Visible Spectrum";
		wavelength = "400-740 Nm wavelength, segmented";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			if (counter <= 1) {
				// red
				c = Color.rgb(255, minRGB, minRGB, alpha);
			} else if (counter > 1 && counter <= 2) {
				// orange
				c = Color.rgb(255, 127, minRGB, alpha);	
			} else if (counter > 2 && counter <= 3) {
				// yellow
				c = Color.rgb(255, 255, minRGB, alpha);	
			} else if (counter > 3 && counter <= 4) {
				// green
				c = Color.rgb(minRGB, 255, minRGB, alpha);	
			} else if (counter > 4 && counter <= 5) {
				// cyan
				c = Color.rgb(minRGB, 255, 255, alpha);	
			} else if (counter > 5 && counter <= 6) {
				// blue
				c = Color.rgb(minRGB, minRGB, 255, alpha);	
			} else if (counter > 6 && counter <= 7) {
				// purple
				c = Color.rgb(255, minRGB, 255, alpha);	
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				dopplerOverflow.add(Color.rgb(255, minRGB, 255, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientH() {
		spectra = "Near Ultraviolet";
		wavelength = "100-400 Nm wavelength";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			int hex = (int) (diff * 255);
			if (counter <= 1) {
				// void black
				c = Color.rgb(0, 0, 0, alpha);	
			} else if (counter > 1 && counter <= 2) {
				// void black to dark blue
				int b = (int) hex/2;
				c = Color.rgb(0, 0, b, alpha);	
			} else if (counter > 2 && counter <= 3) {
				// dark blue to purple
				int b = (int) hex/2;
				hex = 127 + b;
				c = Color.rgb(b, 0, hex, alpha);	
			} else if (counter > 3 && counter <= 4) {
				// purple to fuschia
				hex = 127 + (int) hex/2;
				c = Color.rgb(hex, 0, 255, alpha);	
			} else if (counter > 4 && counter <= 5) {
				// fuschia
				c = Color.rgb(255, 0, 255, alpha);	
			} else if (counter > 5 && counter <= 6) {
				// fuschia to white, delayed
				if (hex<127) {hex = 0;
				} else {hex = (hex-127)*2;}
				if (hex>255) {hex=255;}
				c = Color.rgb(255, hex, 255, alpha);	
			} else if (counter > 6 && counter <= 7) {
				// white
				c = Color.rgb(255, 255, 255, alpha);	
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				dopplerOverflow.add(Color.rgb(255, 255, 255, alpha));	
			}
			colorList.add(c);
		}
	}

	public static void alternativeGradientI() {
		spectra = "Deuteranomaly";
		wavelength = "Red-green colorblind";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			if (counter <= 1) {
				// burnt orange/brown as red
				c = Color.rgb(127, 63, minRGB, alpha);
			} else if (counter > 1 && counter <= 2) {
				// light burnt orange/brown as orange
				c = Color.rgb(127, 127, minRGB, alpha);	
			} else if (counter > 2 && counter <= 3) {
				// yellow
				c = Color.rgb(255, 255, minRGB, alpha);	
			} else if (counter > 3 && counter <= 4) {
				// neon-yellow as green
				c = Color.rgb(190, 255, minRGB, alpha);	
			} else if (counter > 4 && counter <= 5) {
				// cyan, but lighter
				c = Color.rgb(127, 255, 255, alpha);	
			} else if (counter > 5 && counter <= 6) {
				// blue, but darker
				c = Color.rgb(minRGB, minRGB, 190, alpha);	
			} else if (counter > 6 && counter <= 7) {
				// greyish-purple for purple
				c = Color.rgb(190, 127, 190, alpha);	
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				dopplerOverflow.add(Color.rgb(127, 63, 255, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientJ() {
		spectra = "Tritanopia";
		wavelength = "Blue-yellow colorblind";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			if (counter <= 1) {
				// red
				c = Color.rgb(255, minRGB, minRGB, alpha);
			} else if (counter > 1 && counter <= 2) {
				// light red as orange
				c = Color.rgb(255, 127, 127, alpha);	
			} else if (counter > 2 && counter <= 3) {
				// white as yellow
				c = Color.rgb(255, 255, 255, alpha);	
			} else if (counter > 3 && counter <= 4) {
				// cyan as green
				c = Color.rgb(minRGB, 255, 210, alpha);	
			} else if (counter > 4 && counter <= 5) {
				// cyan, but lighter
				c = Color.rgb(127, 255, 255, alpha);	
			} else if (counter > 5 && counter <= 6) {
				// dark, greyed teal as blue
				c = Color.rgb(50, 85, 85, alpha);	
			} else if (counter > 6 && counter <= 7) {
				// slightly greyed pink as purple
				c = Color.rgb(220, 127, 127, alpha);
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				dopplerOverflow.add(Color.rgb(220, 127, 127, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientK() {
		spectra = "Hydrogen";
		wavelength = "Hydrogen emission spectrum";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			int hex = (int) (diff * 255);
			if (counter <= 1) {
				// purple
				c = Color.rgb(127, minRGB, 255, alpha);
			} else if (counter > 1 && counter <= 2) {
				//  purple to blue
				int b = 127 - (int) hex/2;
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(b, minRGB, 255, alpha);	
			} else if (counter > 2 && counter <= 3) {
				// blue
				c = Color.rgb(minRGB, minRGB, 255, alpha);	
			} else if (counter > 3 && counter <= 4) {
				// blue to cyan, accelerated
				if (hex>127) {
					c = Color.rgb(minRGB, 255, 255, alpha);	
				} else {
					hex *= 2;
					if (hex<minRGB) {hex=minRGB;}
					c = Color.rgb(minRGB, hex, 255, alpha);	
				}
			} else if (counter > 4 && counter <= 5) {
				// cyan
				c = Color.rgb(minRGB, 255, 255, alpha);	
			} else if (counter > 5 && counter <= 6) {
				// cyan immediately to red
				if (hex>127) {
					c = Color.rgb(255, minRGB, minRGB, alpha);
				} else {
					c = Color.rgb(minRGB, 255, 255, alpha);	
				}
			} else if (counter > 6 && counter <= 7) {
				// red
				c = Color.rgb(255, minRGB, minRGB, alpha);
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				dopplerOverflow.add(Color.rgb(255, minRGB, minRGB, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientL() {
		spectra = "Nitrogen";
		wavelength = "Nitrogen emission spectrum";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			int hex = (int) (diff * 255);
			if (counter <= 1) {
				// black
				c = Color.rgb(minRGB, minRGB, minRGB, alpha);
			} else if (counter > 1 && counter <= 2) {
				// black to green
				int b = (int) (hex/1.5);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(minRGB, b, minRGB, alpha);
			} else if (counter > 2 && counter <= 3) {
				// green
				c = Color.rgb(minRGB, 190, minRGB, alpha);
			} else if (counter > 3 && counter <= 4) {
				// yellow to orange
				int b = 255 - (int) (hex/2);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(255, b, minRGB, alpha);
			} else if (counter > 4 && counter <= 5) {
				// orange to red
				int b = 127 - (int) (hex/2);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(255, b, minRGB, alpha);
			} else if (counter > 5 && counter <= 6) {
				// red
				c = Color.rgb(255, minRGB, minRGB, alpha);
			} else if (counter > 6 && counter <= 7) {
				// red
				c = Color.rgb(255, minRGB, minRGB, alpha);
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				dopplerOverflow.add(Color.rgb(255, minRGB, minRGB, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientM() {
		spectra = "Stellar Classification";
		wavelength = "Hertzsprung-Russel OBAFGKM";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			int hex = (int) (diff * 255);
			if (counter <= 1) {
				// black to red
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(hex, minRGB, minRGB, alpha);	
			} else if (counter > 1 && counter <= 2) {
				// red to orange
				int b = (int) (hex/2);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(255, b, minRGB, alpha);
			} else if (counter > 2 && counter <= 3) {
				// orange to yellow
				int b = (int) 127 + (hex/2);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(255, b, minRGB, alpha);
			} else if (counter > 3 && counter <= 4) {
				// yellow to white
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(255, 255, hex, alpha);	
			} else if (counter > 4 && counter <= 5) {
				// white to mid-blue
				int b = 255 - (int) (hex/2);
				if (b<minRGB) {b=minRGB;}
				hex = 255 - hex;
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(hex, b, 255, alpha);	
			} else if (counter > 5 && counter <= 6) {
				// mid-blue
				c = Color.rgb(minRGB, 127, 255, alpha);	
			} else if (counter > 6 && counter <= 7) {
				// mid-blue to cyan
				int b = (int) 127 + (hex/2);
				if (b<minRGB) {b=minRGB;}				
				c = Color.rgb(minRGB, b, 255, alpha);	
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				dopplerOverflow.add(Color.rgb(255, 255, minRGB, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientN() {
		spectra = "Topographical";
		wavelength = "Below to above sea level";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; // normalize gradient to total color scale
			int whole = (int) counter; // get magnitude of gradient as an integer
			double diff = counter - whole; // convert decimal to rgb color scale value
			if (diff==0.0) {diff = 1;}
			
			int hex = (int) (diff * 255);
			if (counter <= 1) {
				// black to blue
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(minRGB, minRGB, hex, alpha);	
			} else if (counter > 1 && counter <= 2) {
				// blue
				c = Color.rgb(minRGB, minRGB, 255, alpha);
			} else if (counter > 2 && counter <= 3) {
				// blue to cyan
				if (hex<minRGB) {hex=minRGB;}
				c = Color.rgb(minRGB, hex, 255, alpha);	
			} else if (counter > 3 && counter <= 4) {
				// mid green to faded yellow, accelerated
				if (hex < 127) {
					c = Color.rgb(hex, 127, minRGB, alpha);
				} else {
					c = Color.rgb(127, 127, minRGB, alpha);	
				}
			} else if (counter > 4 && counter <= 5) {
				// aded yellow
				c = Color.rgb(127, 127, minRGB, alpha);	
			} else if (counter > 5 && counter <= 6) {
				// faded yellow to mid orange
				int b = 255 - (int) (hex/2);
				if (b<minRGB) {b=minRGB;}
				c = Color.rgb(b, 127, minRGB, alpha);	
			} else if (counter > 6 && counter <= 7) {
				// mid orange to deep red, delayed
				if (hex < 127) {
					c = Color.rgb(127, 127, minRGB, alpha);	
				} else {
					int b = (int) 255 - (hex/2);
					if (b<minRGB) {b=minRGB;}		
					int a = (int) 127 - (hex/2);
					if (a<minRGB) {a=minRGB;}	
					c = Color.rgb(b, a, minRGB, alpha);	
				}
			} else {
				throw new IndexOutOfBoundsException();
			}
			while (dopplerOverflow.size() < 255) {
				// red
				dopplerOverflow.add(Color.rgb(255, minRGB, minRGB, alpha));	
			}
			colorList.add(c);
		}
	}
	
	public static void alternativeGradientO() {
		spectra = "All Black";
		wavelength = "N/A";
		for (int i = 0; i<GRAD_SCALE; i++) {
			colorList.add(Color.rgb(minRGB, minRGB, minRGB, alpha));
		}
	}
	

	
	public static void drawColorScale(GraphicsContext g) {
		// fill in gradient box
		double vScale = 3.5;
		int i = 0;
		for (; i<colorList.size()*vScale; i++) {
			g.setStroke(colorList.get((int) (i/vScale)));
			g.strokeLine(0, colorList.size()*vScale - i, 40, colorList.size()*vScale - i);
		}
		g.setFill(Color.BLACK);
		g.fillRect(0, colorList.size()*vScale, 40, 500);
		
		// draw outline for gradient box
//		g.setStroke(Color.BLACK);
//		g.strokeRect(0, y-GRAD_SCALE, 40, GRAD_SCALE);

		// draw indicator for actively selected object on scale 
//		if (Sim.getActiveObjectIndex() >= 0) {
//			int activeLine = Sim.getActiveObject().getKelvin();
//			if (activeLine > GRAD_SCALE) {
//				activeLine = GRAD_SCALE;
//			}
//			g.drawLine(10, 600 - activeLine, 30, 600 - activeLine);
//		}
	}
	
	public static Color greyScale(Color rgbColor, boolean invertBrightness) {
		
		// the rgb values in JavaFX colors are from 0.0 to 1.0
		double r = rgbColor.getRed();
		double g = rgbColor.getGreen();
		double b = rgbColor.getBlue();

		double rgbAvg = (r+g+b) / 3;
		
		// invert the brightness on the black -> white scale
		if (invertBrightness) {
			rgbAvg = 1 - rgbAvg;
		}
		
		return Color.color(rgbAvg, rgbAvg, rgbAvg, .8);
	}
	
	public static Color getColor(int index) {
		if (index >= GRAD_SCALE) {
			index = GRAD_SCALE - 1;
		} else if (index < 0) {
			index = 0;
		}
		return colorList.get(index);
	}
	
	public static int getScale() {
		return GRAD_SCALE;
	}
	
	public static Color next() {
		Color temp = getColor(next);
		next++;
		if (next==colorList.size()) {
			next = 0;
		}
		return temp;
	}
	
	public static void resetSchema() {
		colorList.clear();
		alternativeGradientC();
		spectraIndex = 0;
		dopplerShift = 0;
	}
	
	public static void nextSchema(int i) {
		final int schemaMax = 14;
		spectraIndex += i;
	
		// if new index is over the maximum by more than one, go to maximum
		// if new index is below zero by exactly one, go to maximum
		// if new index is over the maximum by exactly one, go to beginning
		// if new index is below zero by more than one, go to beginning
		if (spectraIndex > schemaMax+1 || spectraIndex+1==0) {
			spectraIndex = schemaMax;
		} else if (spectraIndex < 0 || spectraIndex==schemaMax+1) {
			spectraIndex = 0;
		}
		
		colorList.clear();
		dopplerOverflow.clear();
		switch (spectraIndex) {
			case 1:
				alternativeGradientA();
				break;
			case 2:
				alternativeGradientB();
				break;
			case 3:
				alternativeGradientD();
				break;
			case 4:
				alternativeGradientE();
				break;
			case 5:
				alternativeGradientF();
				break;
			case 6:
				alternativeGradientG();
				break;
			case 7:
				alternativeGradientH();
				break;
			case 8:
				alternativeGradientI();
				break;
			case 9:
				alternativeGradientJ();
				break;
			case 10:
				alternativeGradientK();
				break;
			case 11:
				alternativeGradientL();
				break;
			case 12:
				alternativeGradientM();
				break;
			case 13:
				alternativeGradientN();
				break;
			case 14:
				alternativeGradientO();
				break;
			default:
				alternativeGradientC();
		}
	}
	
	// from https://stackoverflow.com/questions/17464906/how-to-list-all-colors-in-javafx
	private static void loadJavaFXColors() {
		try {
		    Class colorClass = Class.forName("javafx.scene.paint.Color");
		    if (colorClass != null) {
		        Field[] fields = colorClass.getFields();
		        for (int i = 0; i < fields.length; i++) {
		            Field f = fields[i];                
		            Object obj = f.get(null);
		            if(obj instanceof Color){
		            	allColors.put(f.getName(), (Color) obj);
		            }
		        }
		    }
		} catch (ClassNotFoundException | IllegalAccessException e1) {
			System.out.println("failed to import javafx default colors");
		}


	}
}
