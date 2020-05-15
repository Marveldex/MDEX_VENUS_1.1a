# MDEX_VENUS_1.1a

This is Sample Application for VENUS bluetooth measurement board & sensor test

It can be operated only in Android environment.

We use BLE 4.0 communication

Venus product :  http://mdex-shop.com/product/detail.html?product_no=93&cate_no=145&display_group=1

/*

< 마블덱스 >  www.mdex.co.kr

본 소스는 2016년에 마지막으로 갱신되었습니다. 당사에 2019년용으로 개정된 소스코드가 있습니다. 최신소스가 필요하신 분은 <sales@mdex 쩜 씨오 쩜 케이알>  로 요청해주세요. 단, 메일에 비너스 보드의 사진을 첨부해주시기 바랍니다.

< Marveldex Inc >  www.mdex.co.kr

The last update of this project is 2016, Jellybean. We have 2019 revision of this source. If you need latest source code, please request to <sales@mdex dot co dot kr > with the photo attachment of Venus circuit board.


*/


비너스용 어플은 노르딕 nrfUART 어플을 기반으로 제작된 소스입니다.

Base project is Nordic UART source code : https://github.com/hubuhubu/Android-nRF-UART


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
