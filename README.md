# MDEX_VENUS_1.1a

This is Sample Application for MDEX_VENUS board & sensor test

It can be operated only in Android environment.

we use BLE 4.0 communication

================================================================

노르딕 nrfUART 소스코드 주소
https://github.com/hubuhubu/Android-nRF-UART

Main UI 
<div align = "center">
<img src="https://github.com/Marveldex/MDEX_VENUS_1.1a/blob/master/Img/MainUi.png" />
</div>


Protocol
<div align = "center">
<img src="https://github.com/Marveldex/MDEX_VENUS_1.1a/blob/master/Img/VENUS_PROTOCAOL1.png" />
</div>
<div align = "center">
<img src="https://github.com/Marveldex/MDEX_VENUS_1.1a/blob/master/Img/VENUS_PROTOCOL2.png" />
</div>



---------------------------------------
2018-3-06

수정사항
 - 블루투스의 자동 재연결 옵션 추가. 비너스 보드 단과 연결이 끊어질 경우, 폰에서 재연결을 시도한다. 재연결을 시도하는 동안 BLE상태는 'Receiving'에서 'Blind'로 바뀐다.

---------------------------------------

### Note
- Android 4.3 or later is required.
- Android Studio supported 
