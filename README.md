# AntiGravity Keymapper (Zero-Detection Mouse & Keyboard Mapper)

Ứng dụng Android kết nối chuột và bàn phím để chơi game (như Panda Mouse Pro, Mantis Mouse Pro) với cơ chế **hoạt động độc lập, không can thiệp bộ nhớ hoặc mã nguồn game**, đảm bảo không bị hệ thống chống gian lận (Anti-Cheat) quét/khóa tài khoản.

---

## 🎮 Các Tính Năng Đã Được Nâng Cấp Hoàn Thiện

1. **Chuẩn Không Bị Phát Hiện & Chống AI Quét (Anti-AI Humanize Touch)**:
   - Sử dụng **ADB Shell Daemon / Shizuku** để inject sự kiện cảm ứng đa điểm trực tiếp vào tầng hệ điều hành.
   - Tích hợp thuật toán **Biological Micro-Jitter (Độ rung sinh học)** và **Đường cong vuốt Bezier tự nhiên** cùng thời gian chạm biến thiên ngẫu nhiên (45ms ~ 75ms), ngăn chặn các hệ thống phân tích AI phát hiện thao tác máy tính.
2. **Ghi Thao Tác Macro & Chạy Vòng Lặp Vô Hạn (Macro Record & Infinite Loop)**:
   - **Ghi lại toàn bộ chuỗi thao tác (Record)**: Ghi lại từng cú chạm, click chuột, di chuyển phím với độ trễ chính xác từng mili-giây.
   - **Chạy vòng lặp vô hạn (Infinite Loop)**: Tự động lặp đi lặp lại chuỗi thao tác vừa ghi mà không dừng lại cho đến khi người dùng bấm phím tắt dừng lại.

## 🎮 Các Tính Năng Cao Cấp Mới Được Bổ Sung:

1. **👀 Trải Nghiệm Full-View & Tùy Chỉnh Độ Mờ Nút (Button Opacity)**:
   - Thanh trượt chỉnh **Độ mờ / Độ trong suốt (10% $\rightarrow$ 100%)**: Giúp bạn làm mờ hoàn toàn các nút phím ảo để có tầm nhìn toàn màn hình 100% không bị che khuất tầm mắt khi ngắm bắn.

2. **🔍 Tăng / Giảm Kích Thước Nút Bấm & Icon (Button Scale)**:
   - Thanh trượt chỉnh **Kích thước nút (50% $\rightarrow$ 200%)**: Tùy chỉnh icon nhỏ gọn gàng hoặc phóng to theo sở thích và độ phân giải màn hình điện thoại.

3. **⚡ Tăng / Giảm Tốc Độ Chuột Toàn Diện (DPI & Global Sensitivity)**:
   - Thanh trượt chỉnh **Tốc độ chuột tổng thể (10% $\rightarrow$ 300%)** cùng 2 thanh trượt vi chỉnh riêng biệt theo **Trục Ngang (X)** và **Trục Dọc (Y)** giúp bạn di chuột xoay góc nhìn mượt mà nhất.

4. **🖱️ Tận Dụng Toàn Diện Con Lăn Chuột (Scroll Wheel Action)**:
   - **Lăn chuột lên (`Scroll UP`)**: Tự động đổi sang Súng 1 (hoặc nhảy).
   - **Lăn chuột xuống (`Scroll DOWN`)**: Tự động đổi sang Súng 2 (hoặc nạp đạn).
   - **Nhấn con lăn chuột (`Middle Click`)**: Ghim vị trí kẻ địch (Ping / Mark) hoặc ném lựu đạn.

5. **⚡ Tự Động Đổi Hồ Sơ Theo Game (Auto-Profile Switching)**:
   - Tự động nhận diện khi bạn mở Free Fire, PUBG Mobile, Liên Quân Mobile, Call of Duty Mobile.

6. **🎯 Mô Phỏng Con Quay Hồi Chuyển (Gyroscope Simulation)**:
   - Dịch chuyển động vi mô của chuột thành thao tác Gyroscope giúp ghìm tâm sấy súng cực đầm.

7. **📁 Xuất & Nhập Cấu Hình Bằng File Cục Bộ (Local .JSON Backup - Không cần Cloud)**:
   - Xuất / Khôi phục file `Tx_Keymap_Config.json` ngay trên máy chỉ với 1 cú chạm.

## 🎮 Bảng Phím Nóng (Tương thích 100% bàn phím 1 tay F1-F6, 1-6, Q-T, A-G, Z-B):

| Phím Tắt | Chức Năng |
| :--- | :--- |
| **`~` (Grave)** hoặc **`Ctrl`** | Khóa / Mở con trỏ chuột FPS (xoay góc nhìn tâm súng) |
| **`F4`** hoặc **`Ctrl + B`** | Bật / Tắt chế độ **Tàng hình (Stealth Mode)** Ẩn toàn bộ giao diện |
| **`F5`** | Bắt đầu / Dừng **Ghi Macro** chuỗi thao tác (Record) |
| **`F6`** | Bật / Dừng **Chạy Vòng Lặp Vô Hạn (Infinite Loop)** chuỗi thao tác vừa ghi |
| **`Lăn Chuột Lên (▲)`** | Đổi sang Súng 1 / Nhảy |
| **`Lăn Chuột Xuống (▼)`** | Đổi sang Súng 2 / Nạp đạn |
| **`Click Con Lăn (●)`** | Ghim / Ping vị trí kẻ địch |
| **`Chuột Trái (LMB)`** | Bắn + Tự động ghìm tâm súng cơ học |
| **`Chuột Phải (RMB)`** | Bật Ngắm + Smart ADS giảm tốc độ chuột bắn tỉa |

6. **Cần Di Chuyển WASD & Nút Nổi Đa Năng**:
   - Mô phỏng cần analog joystick 8 hướng mượt mà.
   - Giao diện nổi cho phép chỉnh phím trực tiếp trong game.

---

## 🚀 Hướng Dẫn Cài Đặt & Sử Dụng

### Bước 1: Cài đặt ứng dụng
File APK được xuất ra tại thư mục:
`app/build/outputs/apk/debug/app-debug.apk`

Cài đặt vào điện thoại bằng lệnh:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Bước 2: Kích hoạt ADB Daemon (Để có độ trễ 0ms & Không bị phát hiện)
1. Bật **Tùy chọn nhà phát triển** (Developer Options) & **Gỡ lỗi USB** (USB Debugging) trên điện thoại Android.
2. Cắm cáp USB vào máy tính.
3. Chạy tập tin:
   - Windows: Click đúp vào `activate.bat`
   - Mac / Linux: Chạy `./activate.sh`

*(Hoặc nếu máy có cài đặt **Shizuku**, bạn có thể kích hoạt không dây trực tiếp trên điện thoại mà không cần PC).*

### Bước 3: Cấp quyền và Bắt đầu Chơi Game
1. Mở ứng dụng **AntiGravity Keymapper**.
2. Cấp quyền **Cửa sổ nổi (Display over other apps)**.
3. Cắm hoặc kết nối Chuột & Bàn phím Bluetooth / USB OTG vào điện thoại.
4. Bấm **Khởi Chạy Keymapper** -> Mở game yêu thích của bạn.
5. Bấm vào bong bóng nổi để kéo thả các phím vào đúng vị trí nút bấm trong game.
6. Chuyển sang chế độ **Chơi Game**, bấm `Ctrl` hoặc `~` để khóa chuột và trải nghiệm!

---

## 🛠️ Cấu Trúc Dự Án
```
APK2/
├── activate.bat                   # Kịch bản kích hoạt Daemon cho Windows
├── activate.sh                    # Kịch bản kích hoạt Daemon cho Linux/Mac
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/antigravity/keymapper/
│   │   │   ├── daemon/ServerDaemon.java      # Daemon chạy quyền ADB
│   │   │   ├── injection/                    # Engine inject cảm ứng
│   │   │   ├── input/                        # Controller WASD & FPS Aim
│   │   │   ├── model/                        # Cấu trúc dữ liệu Keymapping
│   │   │   ├── service/                      # FloatingOverlayService
│   │   │   ├── storage/                      # Lưu trữ Keymap profiles
│   │   │   └── ui/                           # MainActivity & KeymapOverlayView
│   │   └── res/                              # Giao diện & tài nguyên đồ họa
```
