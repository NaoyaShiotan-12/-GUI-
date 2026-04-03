

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

	public static void main(String[] args) {
		//1-1. JFrame (窓)を作成
		JFrame frame = new JFrame("デイリープランナー");

		//1-2.閉じるボタンを押したときにプログラムを終了させる設定
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

		//7-1.カレンダー用のパネル枠を作成
		//GridLayout(行, 列) です。0を指定すると自動で計算してくれます。
		JPanel calendarPanel = new JPanel(new java.awt.GridLayout(0, 7));

		//7-2.曜日ラベルを作成
		String[] days = { "日", "月", "火", "水", "木", "金", "土" };
		for (String d : days) {
			calendarPanel.add(new JLabel(d, JLabel.CENTER));
		}

		//7-3. 1日から３０日までのボタンをループで作成
		for (int i = 1; i <= 30; i++) {
			//11-1.数字を２桁に整えてボタンを作る
			String dayStr = (i < 10) ? "0" + i : String.valueOf(i);
			JButton dayButton = new JButton(dayStr);
			calendarPanel.add(dayButton);

			//10-1.ボタンを押した時の動き
			dayButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					//13-1. 押された日付を取得

					String selectedDay = ((JButton) e.getSource()).getText();
					String fullDate = "2026/04/" + selectedDay;

					//13-2.下の日付ラベルを書き換える
					//１桁の場合は01，02表示にすること！

					dateLabel.setText("【" + fullDate + "】");

					//13-3.【重要】その日のデータが地図にあるか確認。なければ新しく作成
					if (!allData.containsKey(fullDate)) {
						allData.put(fullDate, new DefaultListModel<String>());
					}

					//13-4.画面のリストセット
					taskList.setModel(allData.get(fullDate));
				}

			});
		}

		//8-1.一番上にカレンダーをスクロールできる箱に配置
		JScrollPane calendarScroll = new JScrollPane(calendarPanel);
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

		//1-5.最後に「表示しろ！」と命令する（これがないと映りません）
		frame.setVisible(true);

	}

}

//15-1 付箋クラス
class StickyNote extends javax.swing.JFrame {
    private javax.swing.JTextArea textArea;
    private String fileName;
    private java.awt.Image bgImage; 

    public StickyNote(String content, String id, String dateLabelText) {
        this.fileName = "note_" + id + ".txt";
        setUndecorated(true); 
        setSize(250, 250);
        setAlwaysOnTop(true);

        // 1. 季節判定（ここは以前と同じ）
        java.awt.Color bgColor = new java.awt.Color(255, 255, 180);
        String iconPath = "";
        if (dateLabelText.contains("/03/") || dateLabelText.contains("/04/") || dateLabelText.contains("/05/")) {
            bgColor = new java.awt.Color(255, 220, 230); // 春：ピンク
            iconPath = "./images/spring.jpg";
        } else if (dateLabelText.contains("/06/") || dateLabelText.contains("/07/") || dateLabelText.contains("/08/")) {
            bgColor = new java.awt.Color(220, 240, 255); // 夏：水色
            iconPath = "./images/summer.jpg";
        } else if (dateLabelText.contains("/09/") || dateLabelText.contains("/10/") || dateLabelText.contains("/11/")) {
            bgColor = new java.awt.Color(255, 230, 220); // 秋
            iconPath = "./images/autumn.jpg";
        } else {
            bgColor = new java.awt.Color(245, 235, 230); // 冬
            iconPath = "./images/winter.jpg";
        }

        // 画像読み込み
        java.io.File file = new java.io.File(iconPath);
        if (file.exists()) {
            // 成功した場合
            bgImage = new javax.swing.ImageIcon(file.getAbsolutePath()).getImage();
            System.out.println("【成功】画像を読み込みました: " + file.getAbsolutePath());
        } else {
            // 失敗した場合：プログラムが今見ている場所をコンソールに出力
            System.out.println("【失敗】画像が見つかりません。探している場所はここです：");
            System.out.println(" → " + file.getAbsolutePath());
        }
        // 2. 【重要】背景を描画するパネルの設定
        final java.awt.Color finalBgColor = bgColor;
        javax.swing.JPanel mainPanel = new javax.swing.JPanel(new java.awt.BorderLayout()) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                g.setColor(finalBgColor);
                g.fillRect(0, 0, getWidth(), getHeight());
                if (bgImage != null) {
                    // 右下にイラストを描画
                    g.drawImage(bgImage, getWidth()-90, getHeight()-90, 80, 80, this);
                }
            }
        };

        // 3. 【重要】テキストエリアとスクロールパネルを「透明」にする
        textArea = new javax.swing.JTextArea(content);
        textArea.setOpaque(false); // テキストエリアを透明にする
        textArea.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        textArea.setLineWrap(true);
        
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);
        scrollPane.setOpaque(false); // スクロールパネルを透明にする
        scrollPane.getViewport().setOpaque(false); // 中身のビューポートも透明にする
        scrollPane.setBorder(null); // 枠線を消す

        // マウス操作の設定（ドラッグ移動・右クリック保存）
        var adapter = new java.awt.event.MouseAdapter() {
            private java.awt.Point origin;
            public void mousePressed(java.awt.event.MouseEvent e) { origin = e.getPoint(); }
            public void mouseDragged(java.awt.event.MouseEvent e) {
                java.awt.Point p = getLocation();
                setLocation(p.x + e.getX() - origin.x, p.y + e.getY() - origin.y);
            }
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    saveTofile();
                    dispose();
                }
            }
        };
        textArea.addMouseListener(adapter);
        textArea.addMouseMotionListener(adapter);

        mainPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    public void saveTofile() {
        try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter(fileName))) {
            out.print(textArea.getText());
        } catch (java.io.IOException e) { e.printStackTrace(); }
    }
}

