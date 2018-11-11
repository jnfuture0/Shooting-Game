package shooting;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*; //ArrayList를 위한 import 추가
import java.awt.image.*; //버퍼이미지 클래스 사용을 위한 import 추가
import java.io.*;

public class Frame_make {
	public static void main(String[] ar) {

		game_Frame fms = new game_Frame();
	}
}

//프레임을 만들기 위한 클래스, 키보드 이벤트 처리를 위한 KeyListener 상속, 스레드 돌리기 위한 Runnable 상속
class game_Frame extends JFrame implements KeyListener, Runnable {
	int f_width; // 생성할 프레임 넓이
	int f_height; // 생성할 프레임 높이

	// 캐릭터 좌표 변수
	int x, y;

	int[] cx = { 0, 0, 0 };
	int bx = 0;

	// 키보드 입력 처리 변수
	boolean KeyUp = false;
	boolean KeyDown = false;
	boolean KeyLeft = false;
	boolean KeyRight = false;
	boolean KeySpace = false;

	int cnt; // 각종 타이밍 조절을 위해 무한 루프를 카운터할 변수

	int player_Speed;
	int missile_Speed;
	int fire_Speed;
	int enemy_Speed;
	int player_Status = 0; // 0:평상시, 1:미사일발사, 2:충돌
	int game_Score;
	int player_Hitpoint;

	// 스레드 생성
	Thread th;

	// 이미지 불러오기 위한 툴킷
	Toolkit tk = Toolkit.getDefaultToolkit();

	Image[] Player_img;
	Image BackGround_img;
	Image[] Cloud_img;
	Image[] Explo_img;

	Image me_img;
	Image missile_img;
	Image enemy_img;
	Image missile2_img;

	ArrayList Missile_List = new ArrayList(); // 다수의 미사일 관리하기 위한 배열
	ArrayList Enemy_List = new ArrayList(); // 다수의 적 관리 배열
	ArrayList Explosion_List = new ArrayList(); // 다수의 폭발이펙트 관리배열

	Image buffImage; // 더블 버퍼링용
	Graphics buffg; // 더블 버퍼링용

	Missile ms; // 미사일 클래스 접근 키
	Enemy en;
	Explosion ex;

	game_Frame() {
		init();
		start();

		setTitle("슈팅 게임 만들기");
		setSize(f_width, f_height);

		Dimension screen = tk.getScreenSize();

		int f_xpos = (int) (screen.getWidth() / 2 - f_width / 2);
		int f_ypos = (int) (screen.getHeight() / 2 - f_height / 2);

		setLocation(f_xpos, f_ypos);
		setResizable(false);
		setVisible(true);
	}

	public void init() {
		Sound("c:/Users/s_jnfuture0/eclipse-workspace/shooting/bg.wav", true);
		
		x = 100; // 최초 좌표
		y = 100;
		f_width = 1200;
		f_height = 600;

		missile_img = new ImageIcon("Missile.png").getImage();
		enemy_img = new ImageIcon("Enemy.png").getImage();
		missile2_img = new ImageIcon("Missile2.png").getImage();

		Player_img = new Image[5];
		for (int i = 0; i < Player_img.length; ++i) {
			Player_img[i] = new ImageIcon("f15k_" + i + ".png").getImage();
		}

		BackGround_img = new ImageIcon("background.png").getImage();

		Cloud_img = new Image[3];
		for (int i = 0; i < Cloud_img.length; ++i) {
			Cloud_img[i] = new ImageIcon("cloud_" + i + ".png").getImage();
		}

		Explo_img = new Image[3];
		for (int i = 0; i < Explo_img.length; ++i) {
			Explo_img[i] = new ImageIcon("explo_" + i + ".png").getImage();
		}

		game_Score = 0;
		player_Hitpoint = 3;
		player_Speed = 5;
		missile_Speed = 11;
		fire_Speed = 15; // 연사속도
		enemy_Speed = 7;

	}

	public void start() {
		// 프레임 오른쪽 위의 X버튼 누르면 프로그램 종료
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addKeyListener(this); // 키보드 이벤트 실행
		th = new Thread(this); // 스레드 생성
		th.start(); // 스레드 실행
	}

	public void run() { // 스레드 무한 루프
		try { // 예외옵션 설정
			while (true) {
				KeyProcess(); // 키보드 입력처리를 하여 x,y 갱신
				EnemyProcess(); // 적 움직임 처리 메소드
				MissileProcess(); // 미사일 처리 메소드
				ExplosionProcess();

				repaint(); // 갱신된 x,y 값으로 이미지 새로 그리기
				Thread.sleep(20); // 20milli sec로 스레드 돌리기
				cnt++; // 무한 루프 카운터
			}
		} catch (Exception e) {
		}
	}

	public void MissileProcess() {
		if (KeySpace) {
			player_Status = 1;
			if ((cnt % fire_Speed) == 0) {
				ms = new Missile(x + 150, y + 30, missile_Speed);
				Missile_List.add(ms);
				
				ms = new Missile(x + 150, y + 30, 330, missile_Speed);
				Missile_List.add(ms);

				ms = new Missile(x + 150, y + 30, 30, missile_Speed);
				Missile_List.add(ms);
				
				//Sound("mfire.wav", false);
			}
		}

		for (int i = 0; i < Missile_List.size(); ++i) {
			ms = (Missile) Missile_List.get(i);
			ms.move();
			if (ms.x > f_width - 20 || ms.x < 0 || ms.y < 0 || ms.y > f_height) {
				Missile_List.remove(i);
			}

			if (Crash(x, y, ms.x, ms.y, Player_img[0], missile_img) && ms.who == 1) {
				player_Hitpoint--;

				ex = new Explosion(x, y, 1);

				Explosion_List.add(ex);

				Missile_List.remove(i);
			}

			for (int j = 0; j < Enemy_List.size(); ++j) {
				en = (Enemy) Enemy_List.get(j);
				if (Crash(ms.x, ms.y, en.x, en.y, missile_img, enemy_img) && ms.who == 0) {
					Missile_List.remove(i);
					Enemy_List.remove(j);

					game_Score += 10;

					// 적이 위치해있는 곳의 중심좌표 x,y값과 폭발설정을 받은 값 받음. 0:폭발, 1:피격
					ex = new Explosion(en.x + enemy_img.getWidth(null) / 2, en.y + enemy_img.getHeight(null) / 2, 0);

					Explosion_List.add(ex);
					//Sound("explo.wav", false);
				}
			}
		}
	}

	public void EnemyProcess() {
		for (int i = 0; i < Enemy_List.size(); ++i) {
			en = (Enemy) (Enemy_List.get(i)); // 배열에 적이 생성되어있을때 해당되는 적 판별
			en.move();
			if (en.x < -200) { // 좌표 넘어가면
				Enemy_List.remove(i); // 삭제
			}

			if (cnt % 50 == 0) {
				ms = new Missile(en.x, en.y + 25, 180, missile_Speed, 1);

				Missile_List.add(ms);
			}
			if (Crash(x, y, en.x, en.y, Player_img[0], enemy_img)) {
				player_Hitpoint--;
				Enemy_List.remove(i);
				game_Score += 10;

				// 적 위치해있는곳의 중심좌표 x,y값과 폭발설정 받은 값 받음
				ex = new Explosion(en.x + enemy_img.getWidth(null) / 2, en.y + enemy_img.getHeight(null) / 2, 0);

				// 제거된 적위치에 폭발이펙트
				Explosion_List.add(ex);

				// 적 위치해있는곳 중심좌표와 폭발설정 값
				ex = new Explosion(x, y, 1);

				Explosion_List.add(ex);
			}
		}

		if (cnt % 200 == 0) { // 루프 300회마다
			en = new Enemy(f_width + 100, 100, enemy_Speed);
			Enemy_List.add(en); // 각 좌표로 적을 생성 후 배열에 추가
			en = new Enemy(f_width + 100, 200, enemy_Speed);
			Enemy_List.add(en);
			en = new Enemy(f_width + 100, 300, enemy_Speed);
			Enemy_List.add(en);
			en = new Enemy(f_width + 100, 400, enemy_Speed);
			Enemy_List.add(en);
			en = new Enemy(f_width + 100, 500, enemy_Speed);
			Enemy_List.add(en);
		}
	}

	public void ExplosionProcess() {
		for (int i = 0; i < Explosion_List.size(); ++i) {
			ex = (Explosion) Explosion_List.get(i);
			ex.effect();
		}
	}

	// 충돌판정을 위한 Crash 메소드
	public boolean Crash(int x1, int y1, int x2, int y2, Image img1, Image img2) {
		boolean check = false;

		if (Math.abs((x1 + img1.getWidth(null) / 2) - (x2 + img2.getWidth(null) / 2)) < (img2.getWidth(null) / 2
				+ img1.getWidth(null) / 2)
				&& Math.abs((y1 + img1.getHeight(null) / 2)
						- (y2 + img2.getHeight(null) / 2)) < (img2.getHeight(null) / 2 + img1.getHeight(null) / 2)) {
			check = true;
		} else {
			check = false;
		}

		return check;
	}

	public void paint(Graphics g) {
		// 더블버퍼링 버퍼 크기를 화면 크기와 같게 설정
		buffImage = createImage(f_width, f_height);
		buffg = buffImage.getGraphics();

		update(g);
	}

	public void update(Graphics g) {
		Draw_Background();
		Draw_Player();

		Draw_Missile(); // 그려진 미사일 가져와 실행
		Draw_Enemy(); // 그려진 적 이미지

		Draw_Explosion();
		Draw_StatusText();
		g.drawImage(buffImage, 0, 0, this);
	}

	/*
	 * public void Draw_Char() { buffg.clearRect(0,0,f_width,f_height);
	 * buffg.drawImage(me_img,x,y,this); }
	 */

	public void Draw_Background() {
		buffg.clearRect(0, 0, f_width, f_height);

		if (bx > -500) {
			buffg.drawImage(BackGround_img, bx, 0, this);
			bx -= 1;
		} else {
			bx = 0;
		}

		for (int i = 0; i < cx.length; ++i) {
			if (cx[i] < 1400) {
				cx[i] += 5 + i * 3;
			} else {
				cx[i] = 0;
			}
			// 3개의 구름 각각 다른 속도로 좌측으로 움직임
			buffg.drawImage(Cloud_img[i], 1200 - cx[i], 50 + i * 200, this);
		}
	}

	public void Draw_Player() {
		switch (player_Status) {
		case 0:
			if (((cnt / 5) % 2) == 0) {
				buffg.drawImage(Player_img[1], x, y, this);
			} else {
				buffg.drawImage(Player_img[2], x, y, this);
			}
			break;
		case 1:
			if (((cnt / 5) % 2) == 0) {
				buffg.drawImage(Player_img[3], x, y, this);
			} else {
				buffg.drawImage(Player_img[4], x, y, this);
			}

			player_Status = 0;
			break;
		case 2:
			break;
		}
	}

	public void Draw_Missile() {
		for (int i = 0; i < Missile_List.size(); ++i) { // 미사일 존재유무 확인
			ms = (Missile) (Missile_List.get(i)); // 미사일 위치값 확인

			// 현재좌표에 미사일 그리기.
			if (ms.who == 0)
				buffg.drawImage(missile_img, ms.x, ms.y, this);

			if (ms.who == 1)
				buffg.drawImage(missile2_img, ms.x, ms.y, this);
		}
	}

	public void Draw_Enemy() { // 적 이미지 그리는 부분
		for (int i = 0; i < Enemy_List.size(); ++i) {
			en = (Enemy) (Enemy_List.get(i));
			// 배열에 생성된 각 적을 판별하여 이미지 그리기
			buffg.drawImage(enemy_img, en.x, en.y, this);
		}
	}

	public void Draw_Explosion() {
		for (int i = 0; i < Explosion_List.size(); ++i) {
			ex = (Explosion) Explosion_List.get(i);

			if (ex.damage == 0) { // 설정값 0이면 폭발용 이미지 그리기
				if (ex.ex_cnt < 7) {
					buffg.drawImage(Explo_img[0], ex.x - Explo_img[0].getWidth(null) / 2,
							ex.y - Explo_img[0].getHeight(null) / 2, this);
				} else if (ex.ex_cnt < 14) {
					buffg.drawImage(Explo_img[1], ex.x - Explo_img[1].getWidth(null) / 2,
							ex.y - Explo_img[1].getHeight(null) / 2, this);
				} else if (ex.ex_cnt < 21) {
					buffg.drawImage(Explo_img[1], ex.x - Explo_img[2].getWidth(null) / 2,
							ex.y - Explo_img[2].getHeight(null) / 2, this);
				} else if (ex.ex_cnt > 21) {
					Explosion_List.remove(i);
					ex.ex_cnt = 0;
				}
			} else { // 설정값 1이면 피격용 이미지 그리기
				if (ex.ex_cnt < 7) {
					buffg.drawImage(Explo_img[0], ex.x + 120, ex.y + 15, this);
				} else if (ex.ex_cnt < 14) {
					buffg.drawImage(Explo_img[1], ex.x + 60, ex.y + 5, this);
				} else if (ex.ex_cnt < 21) {
					buffg.drawImage(Explo_img[0], ex.x + 5, ex.y + 10, this);
				} else if (ex.ex_cnt > 21) {
					Explosion_List.remove(i);
					ex.ex_cnt = 0;
				}
			}
		}
	}

	public void Draw_StatusText() {
		buffg.setFont(new Font("Default", Font.BOLD, 20));

		buffg.drawString("SCORE : " + game_Score, 1000, 70);

		buffg.drawString("HitPoint : " + player_Hitpoint, 1000, 90);

		buffg.drawString("Missile Count : " + Missile_List.size(), 1000, 110);

		buffg.drawString("Enemy Count : " + Enemy_List.size(), 1000, 130);
	}

	// 키보드 눌러졌을 때 이벤트 처리
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			KeyUp = true;
			break;
		case KeyEvent.VK_DOWN:
			KeyDown = true;
			break;
		case KeyEvent.VK_LEFT:
			KeyLeft = true;
			break;
		case KeyEvent.VK_RIGHT:
			KeyRight = true;
			break;
		case KeyEvent.VK_SPACE:
			KeySpace = true;
			break;
		}
	}

	// 키보드 눌러졌다가 떼어질 때 이벤트 처리
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			KeyUp = false;
			break;
		case KeyEvent.VK_DOWN:
			KeyDown = false;
			break;
		case KeyEvent.VK_LEFT:
			KeyLeft = false;
			break;
		case KeyEvent.VK_RIGHT:
			KeyRight = false;
			break;
		case KeyEvent.VK_SPACE:
			KeySpace = false;
			break;
		}
	}

	// 키보드 타이핑 될 때
	public void keyTyped(KeyEvent e) {
	}

	// 입력받은 키값을 바탕으로 이동
	public void KeyProcess() {
		if (KeyUp == true) {
			if (y > 20)
				y -= 5;
			player_Status = 0;
		}
		if (KeyDown == true) {
			if (y + Player_img[0].getHeight(null) < f_height)
				y += 5;
			player_Status = 0;
		}
		if (KeyLeft == true) {
			if (x > 0)
				x -= 5;
			player_Status = 0;
		}
		if (KeyRight == true) {
			if (x + Player_img[0].getWidth(null) < f_width)
				x += 5;
			player_Status = 0;
		}
	}
	
	public void Sound(String file, boolean Loop) {
		Clip clip;
		File a = new File(file);
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(a);
			clip = AudioSystem.getClip();
			clip.open(ais);
			clip.start();
			if(Loop) clip.loop(-1);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}

class Enemy {
	int x, y, speed;

	Enemy(int x, int y, int speed) {
		this.x = x;
		this.y = y;
		this.speed = speed;
	}

	public void move() {
		x -= speed;
	}
}

class Missile {
	int x;
	int y;
	int angle;
	int speed;
	int who;

	Missile(int x, int y, int speed) {
		this.x = x;
		this.y = y;
		this.speed = speed;
	}

	Missile(int x, int y, int angle, int speed) {
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.speed = speed;
	}

	Missile(int x, int y, int angle, int speed, int who) {
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.speed = speed;
		this.who = who;
	}

	public void move() {
		x += Math.cos(Math.toRadians(angle)) * speed;
		y += Math.sin(Math.toRadians(angle)) * speed;
	}
}

class Explosion {
	int x, y, ex_cnt, damage;

	Explosion(int x, int y, int damage) {
		this.x = x;
		this.y = y;
		this.damage = damage;
		ex_cnt = 0;
	}

	public void effect() {
		ex_cnt++;
	}
}