
package BinaryFilesPrac;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
 

public class BinaryFilesPrac extends Application {

	/**
	 * Variables
	 */

	// Save And Load Pane and components*/
	private SaveAndLoadPane saveAndLoadPane;
	private File dataFile;
	private File objectFile;
	private ToggleGroup group;
	private Button btnSave;
	private Button btnLoadData;
	private Button btnLoadBookmark;

	// Replace text Pane and components*/
	private ReplaceTextPane replaceTextPane;
	private File textFile;
	private Button btnSelectFile;
	private TextField txtFldFind;
	private TextField txtFldReplaceWith;
	private Button btnReplace;

	// Other variables
	private DataOutputStream dOut;
	private ObjectOutputStream oOut;
	private DataInputStream dIn;
	private ObjectInputStream oIn;
	private boolean isAppendableData;
	private boolean isAppendableObject;
	private Stage stage;

	/**
	 * Application initialization method before start
	 */
	public void init() throws Exception {
		initAppandable();
		initSaveAndLoadPane();
		initReplaceTextPane();
		initDataStream();
		initObjectStream();
	}

	// check if file exists to append objects
	private void initAppandable() {
		dataFile = new File("values.dat");
		isAppendableData = dataFile.exists();
		objectFile = new File("values.obj");
		isAppendableObject = objectFile.exists();
	}

	// init Save And Load Pane
	private void initSaveAndLoadPane() {
		saveAndLoadPane = new SaveAndLoadPane();
		group = saveAndLoadPane.getGroup();
		btnSave = saveAndLoadPane.getBtnSave();
		btnSave.setOnAction(e -> saveValueToFile(group));
		btnLoadData = saveAndLoadPane.getBtnLoadData();
		btnLoadData.setOnAction(e -> loadValueFromFile());
		btnLoadBookmark = saveAndLoadPane.getBtnLoadBookmark();
		btnLoadBookmark.setOnAction(e -> loadBookmarkFromFile());
	}

	// init Replace Text Pane
	private void initReplaceTextPane() {
		replaceTextPane = new ReplaceTextPane();
		btnSelectFile = replaceTextPane.getBtnSelectFile();
		btnSelectFile.setOnAction(e -> selectFile());
		txtFldFind = replaceTextPane.getTxtFldFind();
		txtFldReplaceWith = replaceTextPane.getTxtFldReplaceWith();
		btnReplace = replaceTextPane.getBtnReplaceWith();
		btnReplace.setOnAction(e -> replaceText());
	}

	// select text file
	private void selectFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
		textFile = fileChooser.showOpenDialog(stage);
		if (textFile != null) {
			replaceTextPane.getTxtFldFileName().setText(textFile.getName() + "");
			replaceTextPane.updateStatus("File Selected!", "green");
		} else {
			replaceTextPane.getTxtFldFileName().setText("");
			replaceTextPane.updateStatus("", "black");
		}

	}

	// is valid input method
	private boolean isValidInput() {
		replaceTextPane.updateStatus("", "black");
		boolean isValid = false;
		if (textFile != null) {
			if (txtFldFind == null || txtFldReplaceWith == null)
				replaceTextPane.updateStatus("ERROR! Please Restart the Window!", "red");
			else if (txtFldFind.getText().equals(""))
				replaceTextPane.updateStatus("Enter Text To Find!", "red");
			else {
				isValid = true;
			}
		} else
			replaceTextPane.updateStatus("No Text File Selected!", "red");
		return isValid;
	}

	// reset OutputStream

	private void resetOutputStream(String type) {
		try {
			switch (type) {
			case "Data":
				dOut = new DataOutputStream(new FileOutputStream(dataFile, isAppendableData));
				break;
			case "Object":
				if (isAppendableObject) {
					oOut = new ObjectOutputStream(new FileOutputStream(objectFile, isAppendableObject)) {
						@Override
						protected void writeStreamHeader() throws IOException {
							return;
						}
					};
				} else {
					oOut = new ObjectOutputStream(new FileOutputStream(objectFile));
				}
				break;

			}
		} catch (FileNotFoundException e) {
			System.out.println("resetOutputStream method Exception: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("resetOutputStream method Exception: " + e.getMessage());
		}
	}

	// save Value To File
	private void saveValueToFile(ToggleGroup toggleGroup) {

		if (saveAndLoadPane.getTxtFldValue() == null || saveAndLoadPane.getTxtFldValue().getText().equals("")) {
			saveAndLoadPane.updateStatus("Value can not be empty!", "", "", "red");
		} else {
			RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle();
			String type = selected.getText(), value = saveAndLoadPane.getTxtFldValue().getText();
			boolean isValidBookmark = true;

			try {
				switch (type) {
				case "Boolean":
					dOut.writeUTF(type);
					dOut.writeBoolean(Boolean.parseBoolean(value));
					break;
				case "Char":
					dOut.writeUTF(type);
					dOut.writeChar(value.charAt(0));
					break;
				case "Integer":
					dOut.writeUTF(type);
					dOut.writeInt(Integer.parseInt(value));
					break;
				case "String":
					dOut.writeUTF(type);
					dOut.writeUTF(value);
					break;

				default:
					saveBookmarkToFile();
					break;
				}
				if (isValidBookmark) {
					saveAndLoadPane.updateStatus(type + " saved!", "", "", "green");
					saveAndLoadPane.cleanValueFields();
				}

			} catch (IOException e) {
				saveAndLoadPane.updateStatus(type + " not saved!", "", "", "red");
			} catch (NumberFormatException e) {
				saveAndLoadPane.updateStatus("Entered value must be " + type + "!", "", "", "red");
			} catch (Exception e) {
				saveAndLoadPane.updateStatus(type + " not saved!", "", "", "red");
			}
		}
	}

	// save Bookmark To File
	private boolean saveBookmarkToFile() {
		String addBkMrk="BookMark";
		try {
			if (saveAndLoadPane.getTxtFldTitle() == null || saveAndLoadPane.getTxtFldTitle().getText().equals("")) {
				saveAndLoadPane.updateStatus("Title can not be empty!", "", "", "red");
			} else {
				String title = saveAndLoadPane.getTxtFldTitle().getText(),
						value = saveAndLoadPane.getTxtFldValue().getText();
				oOut.writeUTF(addBkMrk);
				oOut.writeObject(new Bookmark(title, value));
			}
		} catch (IOException e) {
			saveAndLoadPane.updateStatus("Bookmark not saved!", "", "", "red");
		} catch (Exception e) {
			saveAndLoadPane.updateStatus("Bookmark not saved!", "", "", "red");
		}
		return false;
	}

	// reset InputStream
	private void resetInputStream(String type) {
		try {
			switch (type) {
			case "Data":
				dIn = new DataInputStream(new FileInputStream(dataFile));
				break;
			case "Object":
				oIn = new ObjectInputStream(new FileInputStream(objectFile));
				break;
			}
		} catch (FileNotFoundException e) {
			System.out.println("resetInputStream Method Exception: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("resetInputStream Method Exception: " + e.getMessage());
		}
	}

	// load Value From File
	private void loadValueFromFile() {
		String type = "", value = "";
		try {
			if (dIn.available() == 0) {
				resetInputStream("Data");
			}
			if (dIn.available() != 0) {
				type = dIn.readUTF();
				switch (type) {
				case "Boolean":
					value = String.valueOf(dIn.readBoolean());
					break;
				case "Char":
					value = String.valueOf(dIn.readChar());
					break;
				case "Integer":
					value = String.valueOf(dIn.readInt());
					break;
				case "String":
					value = dIn.readUTF();
					break;

				default:

					break;
				}
				saveAndLoadPane.setRButtonSelected(type);
				saveAndLoadPane.updateStatus("Data loaded!", type, value, "green");
			} else {
				saveAndLoadPane.updateStatus("There is no data to load!", "", "", "red");
			}
		} catch (IOException e) {
			saveAndLoadPane.updateStatus("Data not loaded!", "", "", "red");
		} catch (Exception e) {
			saveAndLoadPane.updateStatus("Data not loaded!", "", "", "red");
		}
	}

	// load Bookmark From File
	private void loadBookmarkFromFile() {
		String type = "", value = "";
		try {
			if (oIn.available() == 0) {
				resetInputStream("Object");
			}
			if (oIn.available() != 0) {
				oIn.readUTF();
				Bookmark bkMark = (Bookmark) oIn.readObject();
				type = bkMark.getTitle();
				value = bkMark.getValue();
				saveAndLoadPane.setRButtonSelected(type);
				saveAndLoadPane.updateStatus("Object Loaded!", type, value, "green");
			} else {
				saveAndLoadPane.updateStatus("There is no object to load!", "", "", "red");
			}
		} catch (IOException e) {
			e.printStackTrace();
			saveAndLoadPane.updateStatus("Object not loaded!", "", "", "red");
		} catch (Exception e) {
			e.printStackTrace();
			saveAndLoadPane.updateStatus("Object not loaded!", "", "", "red");
		}
	}

	// replace "find" to "replacewith" string
	// the two way for soltulotion by grigory lessons
	private void replaceText() {
		boolean isFound = false;
		if (isValidInput()) {
			String find = txtFldFind.getText(), replaceWith = txtFldReplaceWith.getText();
			byte findByte[] = new byte[find.length()];
			int readPointer = 0;
			try (RandomAccessFile raf = new RandomAccessFile(textFile, "rw")) {
				while (raf.read(findByte) != -1) {
					String data = new String(findByte);
					if (data.equals(find)) {
						byte copy[] = new byte[(int) raf.length() - find.length() - readPointer];
						raf.read(copy);
						raf.setLength(readPointer);
						raf.write(replaceWith.getBytes());
						raf.write(copy);
						isFound = true;
						readPointer = readPointer + replaceWith.length();
						raf.seek(readPointer);
					} else {
						readPointer++;
						raf.seek(readPointer);
					}
				}
				if (isFound) {
					replaceTextPane.updateStatus("String Replaced!", "green");
					replaceTextPane.cleanValueFields();
				} else
					replaceTextPane.updateStatus("No String Found!", "red");
			} catch (FileNotFoundException e) {
				System.out.println("DeleteStrFromFileMethodException: File Not Found! " + e.getMessage());
			} catch (IOException e) {
				System.out.println("DeleteStrFromFileMethodException: Input Output Exception! " + e.getMessage());
			}

		}

	}

	// init Data Stream
	private void initDataStream() {
		resetOutputStream("Data");
		resetInputStream("Data");
	}

	// init Object stream
	private void initObjectStream() {
		resetOutputStream("Object");
		resetInputStream("Object");
	}

	/**
	 * Application start method
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			stage = primaryStage;
			stage.setScene(new Scene(saveAndLoadPane, 500, 500));
			stage.setTitle("Data And Object Saver");
			stage.show();
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent arg0) {
					try {
						oOut.close();
						dOut.close();
						dIn.close();
						oIn.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			});
			Stage secondStage = new Stage();
			secondStage.setScene(new Scene(replaceTextPane, 500, 500));
			secondStage.setTitle("Text Editor");
			secondStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param args parameter for main method
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
