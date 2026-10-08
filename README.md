# Smart Scan (Simple Java Swing)

เวอร์ชันนี้ลดจำนวนไฟล์ให้คล้ายโครงสร้าง SC_GUI เดิม และไม่ใช้ Maven

## ไฟล์หลัก
- `App.java` จุดเริ่มโปรแกรม
- `SmartScanGUI.java` รวม GUI 4 หน้า: Home / Scan / Review / Payment
- `classes/Product.java`
- `classes/CartItem.java`
- `classes/ShoppingCart.java`
- `classes/ProductCatalog.java`
- `classes/BarcodeScanner.java`
- `classes/BufferedImageLuminanceSource.java`
- `classes/QRCodeGenerator.java`
- `products.csv` ข้อมูลสินค้า
- `images/` รูปสินค้า

## วิธีรันบน Windows / VS Code
1. เปิดโฟลเดอร์นี้ใน VS Code
2. เปิด Terminal
3. ครั้งแรกพิมพ์ `./download-libs.bat` หรือ `download-libs.bat`
4. พิมพ์ `./run.bat` หรือ `run.bat`

## เพิ่มสินค้า
แก้ `products.csv` ตามรูปแบบ:
`id,barcode,name,price,description,stock,imagePath`

เลข barcode ต้องตรงกับบาร์โค้ดจริงที่จะใช้สแกน

## QR Payment
QR ในเวอร์ชันนี้เป็น DEMO QR สำหรับแสดง flow ของโปรแกรม ยังไม่ได้เชื่อม PromptPay หรือธนาคารจริง

## Discount
แสดงส่วนลดไว้แล้ว แต่ `ShoppingCart.getDiscount()` ยังคืนค่า 0.0 เพื่อให้เพิ่มเงื่อนไขทีหลังได้ง่าย


## Thai font / ภาษาไทย
เวอร์ชันนี้ตั้งค่า Swing ให้ใช้ Tahoma (หรือฟอนต์ Windows ที่รองรับภาษาไทย) และอ่าน `products.csv` แบบ UTF-8 เพื่อให้ชื่อสินค้าและรายละเอียดภาษาไทยแสดงได้ถูกต้อง

## Folder naming fix
Java source classes are in `classes/` (package `classes`). External JAR files are in `lib/`.
This avoids the Windows/VS Code conflict between folders named `Lib` and `lib`.

## PromptPay QR (real payment)
Open `classes/QRCodeGenerator.java` and edit only:
`private static final String PROMPTPAY_ID = "PUT_YOUR_PROMPTPAY_ID_HERE";`
Use a 10-digit Thai phone number or 13-digit National ID that is registered with PromptPay.
The QR includes the cart total. Test with a very small amount first and confirm the receiver name/amount in your banking app before paying.
This project generates the QR locally; it does NOT verify that payment was completed. The 5-minute UI timer also does not invalidate the PromptPay QR at the banking network level.

GUI update (Home + Scan)
- Uses provided icons under icon/home and icon/scan.
- Home: moved Smart Scan branding to bottom, fixed intro layout, icon boxes for barcode/scan/cart, green Start button with scan2 icon.
- Scan: circular icon buttons, rounded scan frame, compact Scanner ready pill under frame.
- Empty scan basket collapses to header only; total, Review Order and empty-message area stay hidden until an item is scanned.
