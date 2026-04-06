package days;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class Main {
	//12-1.日付(String)をキーにしてその日のリストを保存する地図（Map）
	private static java.util.Map<String, DefaultListModel<String>> allData = new java.util.HashMap<>();
	private static java.util.List<NoteData> notes = new java.util.ArrayList<>();
	private static int currentYear = 2026;
	private static int currentMonth = 4;
	private static JPanel calendarPanel = new JPanel(new java.awt.GridLayout(0, 7)); // ここで作っておく
	private static JLabel monthLabel = new JLabel("", JLabel.CENTER); // 年月表示用

	public static void main(String[] args) {
		loadData();
		//1-1. JFrame (窓)を作成
		JFrame frame = new JFrame("デイリープランナー");

		//1-2.閉じるボタンを押したときにプログラムを終了させる設定
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				saveData(); // ★ここ重要
				frame.dispose();

			}
		});

		//1-3.サイズ（幅450，高さ400）を決める
		frame.setSize(450, 400);

		//1-4.レイアウト　（部品の並び方）を「東西南北」方式に設定
		frame.setLayout(new BorderLayout());

		//2-1.	部品をまとめるパネル（トレイのようなもの）を作る
		JPanel inputPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

		//2-2.文字を入力する欄を作る（数字はマスの幅）
		JTextField hourField = new JTextField(5); //時間用
		JTextField minuteField = new JTextField(5);

		JTextField taskField = new JTextField(10); // 予定用

		//2-3.実行ボタンを作る
		JButton addButton = new JButton("追加");

		//6-1 今日の日付を取得して文字にする（2025/06/20）
		java.time.LocalDate today = java.time.LocalDate.now();
		String dateStr = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));

		// 日付を表示するラベルを作成（少し太文字でおしゃれに）
		JLabel dateLabel = new JLabel("【" + dateStr + "】");
		dateLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

		//2-4.パネルにラベルと入力欄、ボタンを順番に乗せる
		inputPanel.add(dateLabel); //6-2. 最初に追加
		inputPanel.add(hourField);
		inputPanel.add(new JLabel("時"));
		inputPanel.add(minuteField);
		inputPanel.add(new JLabel("分"));
		inputPanel.add(taskField);
		inputPanel.add(addButton);

		//2-5.最後に、このパネルをフレームの「上に」配置する
		frame.add(inputPanel, BorderLayout.NORTH);

		//3-1.リストの「中身（データ)」を管理するモデルを作る
		//これにデータを追加すると、画面の表示も自動で変わります
		DefaultListModel<String> listModel = new DefaultListModel<>();

		// 3-2.リストを表示する「棚」を作る(上とセット)
		JList<String> taskList = new JList<>(listModel);

		//3-3.スクロールできるように「スクロールパネル」に入れる
		// 予定が増えても、これがあれば上下に動かせる
		JScrollPane scrollPane = new JScrollPane(taskList);

		//3-4.フレームの「中央」に配置する
		frame.add(scrollPane, BorderLayout.CENTER);

		//4-1.「追加」ボタンが押された時の処理（アクションリスナー）を登録
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//4-2.　入力欄(TextField)から文字を抜き出す
				String hour = hourField.getText();
				String minute = minuteField.getText();
				String task = taskField.getText();

				DefaultListModel<String> currentModel = (DefaultListModel<String>) taskList.getModel();

				if (currentModel == null) {
					currentModel = new DefaultListModel<>();
					taskList.setModel(currentModel);
				}

				//4-3.入力チェックとリストへの追加
				if (!hour.isEmpty() && !minute.isEmpty() && !task.isEmpty()) {

					//　時　分　予定　という形式でリストに追加

					currentModel.addElement(hour + "時" + minute + "分-" + task);
					saveData();
					//入力欄をクリア
					hourField.setText("");
					minuteField.setText("");
					taskField.setText("");
				} else {
					JOptionPane.showMessageDialog(frame, "すべて入力してください");
				}

			}

		});//ここでActionListenerを閉じる

		//5-1.削除ボタンを作成
		JButton deleteButton = new JButton("選択した予定を削除");

		//5-2.ボタンをフレームの下に配置
		frame.add(deleteButton, BorderLayout.SOUTH);

		//5-3.削除ボタンが押された時の処理
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (taskList.getModel() instanceof DefaultListModel) {
					DefaultListModel<String> currentModel = (DefaultListModel<String>) taskList.getModel();
					//現在リストで選択されている行の番号（インデックス）取得
					int selectedIndex = taskList.getSelectedIndex();

					//もし何かが選択されていたら（選択されていないときは-1になる）
					if (selectedIndex != -1) {
						//データモデルからその行を削除
						currentModel.remove(selectedIndex);
						saveData();
					} else {
						//何も選んでいない状態で押されたら案内を出す
						JOptionPane.showMessageDialog(frame, "削除する予定を選んでください");

					}
				} else {
					// 日付が選ばれていない場合
					JOptionPane.showMessageDialog(frame, "まずカレンダーの日付をクリックしてください");
				}
			}

		});

		// 15-2. 付箋化ボタンを作成
		JButton stickButton = new JButton("付箋として切り出す");
		inputPanel.add(stickButton); // 入力欄の並びに追加

		// 15-3. ボタンを押した時の動き
		stickButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				// ★ 複数選択された項目をすべて取得する
				java.util.List<String> selectedValues = taskList.getSelectedValuesList();
				String currentDay = dateLabel.getText();

				if (!selectedValues.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					sb.append("日付: ").append(currentDay).append("\n");
					sb.append("予定:\n");
					// 選択された項目をループでつなげる
					for (String value : selectedValues) {
						sb.append(" ・ ").append(value).append("\n");
					}
					sb.append("----------\n");

					String id = String.valueOf(System.currentTimeMillis());
					new StickyNote(sb.toString(), id, currentDay); // 引数に日付を追加
				} else {
					JOptionPane.showMessageDialog(frame, "リストから項目を選択してください");
				}
			}
		});

		// --- 新しいカレンダー生成処理 (月移動対応) ---
		// 7-1. 月移動ボタンなどの「ヘッダー」を作る
		JPanel fullCalendarPanel = new JPanel(new BorderLayout());
		JPanel headerPanel = new JPanel();
		JButton prevBtn = new JButton("< 前月");
		JButton nextBtn = new JButton("次月 >");
		monthLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

		headerPanel.add(prevBtn);
		headerPanel.add(monthLabel);
		headerPanel.add(nextBtn);

		fullCalendarPanel.add(headerPanel, BorderLayout.NORTH);
		fullCalendarPanel.add(calendarPanel, BorderLayout.CENTER); // 冒頭で作ったcalendarPanelを使う

		// 7-2. カレンダーを更新する命令 (Runnable)
		Runnable updateCalendar = () -> {
			calendarPanel.removeAll(); // 一旦クリア
			monthLabel.setText(currentYear + "年 " + currentMonth + "月");

			// 曜日の見出しを追加
			String[] dayNames = { "日", "月", "火", "水", "木", "金", "土" };
			for (String d : dayNames)
				calendarPanel.add(new JLabel(d, JLabel.CENTER));

			// その月の「1日」の曜日と「末日」を計算
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.set(currentYear, currentMonth - 1, 1);
			int startDay = cal.get(java.util.Calendar.DAY_OF_WEEK);
			int lastDay = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

			// 1日が始まるまでの空白
			for (int i = 1; i < startDay; i++)
				calendarPanel.add(new JLabel(""));

			// 日付ボタンを作成
			for (int i = 1; i <= lastDay; i++) {
			    String dStr = (i < 10) ? "0" + i : String.valueOf(i);

			    String mStr = (currentMonth < 10) ? "0" + currentMonth : "" + currentMonth;
			    String fullDate = currentYear + "/" + mStr + "/" + dStr;

			    JButton dayButton = new JButton(dStr);

			    // ★ 予定がある日をチェック
			    boolean hasData = allData.containsKey(fullDate) && allData.get(fullDate).size() > 0;

			    // ★ ここで文字色を赤にする
			    if (hasData) {
			        dayButton.setForeground(java.awt.Color.RED);
			    }

			    dayButton.addActionListener(e -> {
			        String fullDateClicked = currentYear + "/" + mStr + "/" + dStr;
			        dateLabel.setText("【" + fullDateClicked + "】");

			        if (!allData.containsKey(fullDateClicked)) {
			            allData.put(fullDateClicked, new DefaultListModel<>());
			        }
			        taskList.setModel(allData.get(fullDateClicked));
			    });

			    calendarPanel.add(dayButton);
			}
			calendarPanel.revalidate();
			calendarPanel.repaint();
		};

		// 7-3. 移動ボタンの動作設定
		prevBtn.addActionListener(e -> {
			currentMonth--;
			if (currentMonth < 1) {
				currentMonth = 12;
				currentYear--;
			}
			updateCalendar.run();
		});
		nextBtn.addActionListener(e -> {
			currentMonth++;
			if (currentMonth > 12) {
				currentMonth = 1;
				currentYear++;
			}
			updateCalendar.run();
		});

		// 最初に一度実行して表示
		updateCalendar.run();

		//8-1.一番上にカレンダーをスクロールできる箱に配置
		JScrollPane calendarScroll = new JScrollPane(fullCalendarPanel);
		calendarScroll.setPreferredSize(new java.awt.Dimension(450, 250));
		frame.add(calendarScroll, BorderLayout.NORTH);

		//8-2. 真ん中は今のままリスト表示(念のため)
		frame.add(scrollPane, BorderLayout.CENTER);

		//8-3.一番下に入力ボタンと削除ボタンをまとめて配置
		//これらをセットするための新しいパネル
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(inputPanel, BorderLayout.NORTH); //入力欄を上
		southPanel.add(deleteButton, BorderLayout.SOUTH); //削除ボタンを下に

		frame.add(southPanel, BorderLayout.SOUTH);

		//9-1.　画面全体のサイズを、中身が全部入る大きさに設定
		frame.setSize(650, 800);

		//14-1起動時に今日の日付のボタンをプログラムからクリックさせる
		taskList.setModel(new DefaultListModel<>());

		// --- 15. 【統合版】アニメーションして付箋をめくる glassPane ---
		final int[] fSize = { 60 }; // 配列にすることでタイマー内から書き換え可能にする
		JPanel glassPane = new JPanel() {
			@Override
			protected void paintComponent(java.awt.Graphics g) {
				super.paintComponent(g);
				java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
				// アンチエイリアス（線を綺麗にする設定）
				g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
						java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

				// 左上に「めくり角」を描画
				g2.setColor(new java.awt.Color(180, 180, 180, 200)); // 少し濃いめのグレー
				int[] x = { 0, fSize[0], 0 };
				int[] y = { 0, 0, fSize[0] };
				g2.fillPolygon(x, y, 3);

				// 縁取り
				g2.setColor(java.awt.Color.GRAY);
				g2.drawPolygon(x, y, 3);
			}

			@Override
			public boolean contains(int x, int y) {
				// クリックに反応する範囲（通常時 60ピクセル以内）
				return (x + y < 60);
			}
		};

		// --- 15. 【修正版】リスト選択にも対応した「めくり」アクション ---
		// --- 15. 【修正版】めくり側のテキスト構成をボタンと統一する ---
		glassPane.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				String task = taskField.getText();
				java.util.List<String> selected = taskList.getSelectedValuesList();

				if (!task.isEmpty() || !selected.isEmpty()) {
					// アニメーション実行
					new javax.swing.Timer(10, ev -> {
						fSize[0] += 50;
						glassPane.repaint();
						if (fSize[0] > 300) {
							((javax.swing.Timer) ev.getSource()).stop();
							fSize[0] = 60;
							glassPane.repaint();
						}
					}).start();

					// ★ ここが修正ポイント：テキストの構成をボタン側と合わせる
					StringBuilder sb = new StringBuilder();
					sb.append("日付: ").append(dateLabel.getText()).append("\n"); // 日付を追加

					if (!task.isEmpty()) {
						// 入力欄の内容を使う場合（時間も入れる）
						String h = hourField.getText();
						String m = minuteField.getText();
						sb.append("予定: ").append(h).append("時").append(m).append("分 - ").append(task).append("\n");

						// 入力をクリア
						taskField.setText("");
						hourField.setText("");
						minuteField.setText("");
					} else {
						// リスト選択を使う場合
						sb.append("予定一覧:\n");
						for (String s : selected)
							sb.append(" ・ ").append(s).append("\n");
					}

					// 付箋を作成
					String id = String.valueOf(System.currentTimeMillis());
					new StickyNote(sb.toString() + "----------\n", id, dateLabel.getText());
				} else {
					JOptionPane.showMessageDialog(frame, "内容を入力するか、リストを選択してください");
				}
			}
		});

		glassPane.setOpaque(false);
		frame.setGlassPane(glassPane);
		glassPane.setVisible(true);

		//1-5.最後に「表示しろ！」と命令する（これがないと映りません）
		frame.setVisible(true);

	}

	private static void saveData() {

		try {
			java.io.File dir = new java.io.File("data");
			if (!dir.exists()) {
				dir.mkdir(); // フォルダ作成
			}

			try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
					new java.io.FileOutputStream("data/schedule.dat"))) {

				java.util.Map<String, java.util.List<String>> simpleMap = new java.util.HashMap<>();

				for (String key : allData.keySet()) {
					DefaultListModel<String> model = allData.get(key);
					java.util.List<String> list = new java.util.ArrayList<>();

					for (int i = 0; i < model.size(); i++) {
						list.add(model.get(i));
					}
					simpleMap.put(key, list);
				}

				out.writeObject(simpleMap);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static void loadData() {
		try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(
				new java.io.FileInputStream("data/schedule.dat"))) {

			java.util.Map<String, java.util.List<String>> simpleMap = (java.util.Map<String, java.util.List<String>>) in
					.readObject();

			allData = new java.util.HashMap<>();

			for (String key : simpleMap.keySet()) {
				DefaultListModel<String> model = new DefaultListModel<>();
				for (String item : simpleMap.get(key)) {
					model.addElement(item);
				}
				allData.put(key, model);
			}

		} catch (Exception e) {
			allData = new java.util.HashMap<>();
		}
	}

}

//15-1 付箋クラス
class StickyNote extends javax.swing.JFrame {
	private javax.swing.JTextPane textArea; // JTextArea から JTextPane に変更
	private String fileName;
	private java.awt.Image bgImage;

	public StickyNote(String content, String id, String dateLabelText) {
		this.fileName = "note_" + id + ".txt";
		this.textArea = new javax.swing.JTextPane(); // 入力欄を作成
		this.textArea.setText(content);

		setUndecorated(true);
		setSize(250, 250);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

		// --- 右クリックメニューの作成 ---
		javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
		String[] labels = { "至急", "随時", "報告用" };

		for (String label : labels) {
			javax.swing.JMenuItem item = new javax.swing.JMenuItem("【" + label + "】");
			item.addActionListener(ev -> {
				// ★ 赤色・サイズ18・太字でラベルを挿入する
				insertStyledText("【" + label + "】\n", java.awt.Color.RED, 18, true);
				saveTofile();
			});
			popup.add(item);
		}

		// 保存して閉じるボタン
		javax.swing.JMenuItem closeItem = new javax.swing.JMenuItem("保存して付箋を閉じる");
		closeItem.addActionListener(ev -> {
			saveTofile();
			dispose();
		});
		popup.addSeparator();
		popup.add(closeItem);

		textArea.setComponentPopupMenu(popup);
		textArea.setBackground(new java.awt.Color(255, 255, 180));

		// --- (季節のイラスト判定コード：以前のまま) ---
		java.awt.Color bgColor = new java.awt.Color(255, 255, 180);
		String iconPath = "";
		if (dateLabelText.contains("/03/") || dateLabelText.contains("/04/") || dateLabelText.contains("/05/")) {
			bgColor = new java.awt.Color(255, 220, 230);
			iconPath = "/images/spring.jpg";
		} else if (dateLabelText.contains("/06/") || dateLabelText.contains("/07/") || dateLabelText.contains("/08/")) {
			bgColor = new java.awt.Color(220, 240, 255);
			iconPath = "/images/summer.jpg";
		} else if (dateLabelText.contains("/09/") || dateLabelText.contains("/10/") || dateLabelText.contains("/11/")) {
			bgColor = new java.awt.Color(255, 230, 220);
			iconPath = "/images/autumn.jpg";
		} else {
			bgColor = new java.awt.Color(245, 235, 230);
			iconPath = "/images/winter.jpg";
		}
		try {
			java.net.URL imgURL = getClass().getResource(iconPath);
			if (imgURL != null)
				bgImage = new javax.swing.ImageIcon(imgURL).getImage();
		} catch (Exception e) {
		}

		final java.awt.Color finalBgColor = bgColor;
		javax.swing.JPanel mainPanel = new javax.swing.JPanel(new java.awt.BorderLayout()) {
			@Override
			protected void paintComponent(java.awt.Graphics g) {
				super.paintComponent(g);
				g.setColor(finalBgColor);
				g.fillRect(0, 0, getWidth(), getHeight());
				if (bgImage != null)
					g.drawImage(bgImage, getWidth() - 90, getHeight() - 90, 80, 80, this);
			}
		};

		textArea.setOpaque(false);
		javax.swing.JScrollPane sp = new javax.swing.JScrollPane(textArea);
		sp.setOpaque(false);
		sp.getViewport().setOpaque(false);
		sp.setBorder(null);

		var adapter = new java.awt.event.MouseAdapter() {
			private java.awt.Point origin;

			public void mousePressed(java.awt.event.MouseEvent e) {
				origin = e.getPoint();
			}

			public void mouseDragged(java.awt.event.MouseEvent e) {
				java.awt.Point p = getLocation();
				setLocation(p.x + e.getX() - origin.x, p.y + e.getY() - origin.y);
			}
		};
		textArea.addMouseListener(adapter);
		textArea.addMouseMotionListener(adapter);

		mainPanel.add(sp, java.awt.BorderLayout.CENTER);
		add(mainPanel);
		setVisible(true);
	}

	// ★ 文字に色とサイズを付けて挿入するメソッド
	private void insertStyledText(String text, java.awt.Color color, int size, boolean bold) {
		javax.swing.text.StyledDocument doc = textArea.getStyledDocument();
		javax.swing.text.SimpleAttributeSet attr = new javax.swing.text.SimpleAttributeSet();
		javax.swing.text.StyleConstants.setForeground(attr, color);
		javax.swing.text.StyleConstants.setFontSize(attr, size);
		javax.swing.text.StyleConstants.setBold(attr, bold);
		try {
			doc.insertString(0, text, attr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveTofile() {
		try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter(fileName))) {
			out.print(textArea.getText());
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}
}

class NoteData implements java.io.Serializable {
	String text;
	int x;
	int y;

	public NoteData(String text, int x, int y) {
		this.text = text;
		this.x = x;
		this.y = y;
	}
}
