package main.gui;

import java.util.LinkedList;
import java.util.Stack;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Gradient {
	
	/**Defines and generates the gradients used to represent spectra for
	 * obversation channels. Respresentation is demonstrative and not precise.
	 * Gradients generate a specific number of colors.
	 * 
	 * Gradients roughly map to the ranges of stellar temperatures (kelvin), such that
	 * the top of the gradient is arbitrarily hot and the bottom is arbitrarily
	 * "cool". Arbitrary by whatever temperature range is produced in the simulation.
	 * Except when too hot, all temperatures will have an indexed position in the
	 * colorList. This is a poor implementation b/c Gradients are stored manually
	 * rather than expressed algorithmically.
	 */
	
	public static final double COLOR_SCALE = 50.0; // pixel length of a sub-gradient within a spectra gradient
	public static final int SUB_GRADIENT_COUNT = 7; // number of sub-gradients within a spectra gradient
	public static final int GRAD_SCALE = (int) COLOR_SCALE * SUB_GRADIENT_COUNT; // total pixel size of the spectra gradient
	public static LinkedList<Color> colorList = new LinkedList<Color>(); // maintains list of rgb values representing the gradient
	public static final int minRGB = 0; // minimum allowable value for r, g, and b. 0 - full saturation, 255 - all white
	public static final double alpha = 1; // alpha value for all gradients. 0 - clear coloring, 1 - solid coloring
	private static int spectraIndex = 0; // case number for the current spectra
	private static String spectra; // name of the current spectra
	private static String wavelength; // wavelength info for the current spectra
	private static int dopplerShift; // number of pixels to shift by for doppler effect [-COLOR_SCALE, ..., COLOR_SCALE]
	public static Stack<Color> dopplerOverflow = new Stack<Color>(); // store colors not in use due to doppler shift
	
	public static void dopplerShift(int shift) {	
		int scaleEffect = 40;
		
		int diff = Math.abs(scaleEffect * shift - dopplerShift);
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
	}

	public static void alternativeGradientB() {
		spectra = "Long Wave UVA";
		wavelength = "300-550 Nm wavelength";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; 
			int whole = (int) counter; 
			double diff = counter - whole; 
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
			double counter = i / COLOR_SCALE; 
			int whole = (int) counter; 
			double diff = counter - whole; 
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

	public static void alternativeGradientC() {
		spectra = "Long-Infrared";
		wavelength = "8-14 Mm wavelength";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; 
			int whole = (int) counter; 
			double diff = counter - whole; 
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
			double counter = i / COLOR_SCALE; 
			int whole = (int) counter; 
			double diff = counter - whole; 
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
			double counter = i / COLOR_SCALE; 
			int whole = (int) counter; 
			double diff = counter - whole; 
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
			double counter = i / COLOR_SCALE; 
			int whole = (int) counter; 
			double diff = counter - whole; 
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
	
	public static void alternativeGradientH() {
		spectra = "Near Ultraviolet";
		wavelength = "100-400 Nm wavelength";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; 
			int whole = (int) counter; 
			double diff = counter - whole; 
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
	
	public static void alternativeGradientK() {
		spectra = "Hydrogen";
		wavelength = "Hydrogen emission spectrum";
		Color c;
		for (int i = 0; i<GRAD_SCALE; i++) {
			double counter = i / COLOR_SCALE; 
			int whole = (int) counter; 
			double diff = counter - whole; 
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
			double counter = i / COLOR_SCALE; 
			int whole = (int) counter; 
			double diff = counter - whole; 
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
			double counter = i / COLOR_SCALE; 
			int whole = (int) counter; 
			double diff = counter - whole; 
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

	
	public static void drawColorScale(GraphicsContext g) {
		// draw the narrow, vertical color bar for the user interface
		double vScale = 3.5;
		int i = 0;
		for (; i<colorList.size()*vScale; i++) {
			g.setStroke(colorList.get((int) (i/vScale)));
			g.strokeLine(0, colorList.size()*vScale - i, 40, colorList.size()*vScale - i);
		}
		g.setFill(Color.BLACK);
		g.fillRect(0, colorList.size()*vScale, 40, 500);
	}
	
	public static Color getColor(int index) {
		
		if (index >= GRAD_SCALE) {
			index = GRAD_SCALE - 1;
		} else if (index < 0) {
			index = 0;
		}
		return colorList.get(index);
	}
	
	
	public static String getSpectra() {
		return spectra;
	}
	
	public static String getWavelength() {
		return wavelength;
	}
	
	public static void resetSpectra() {
		selectSchema(0);
	}
	
	public static void selectSchema(int target) {
		spectraIndex = target;		
		dopplerShift = 0;
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
				alternativeGradientH();
				break;
			case 7:
				alternativeGradientK();
				break;
			case 8:
				alternativeGradientL();
				break;
			case 9:
				alternativeGradientM();
				break;
			default:
				alternativeGradientC();
				spectraIndex = 0;
		}	}


}
