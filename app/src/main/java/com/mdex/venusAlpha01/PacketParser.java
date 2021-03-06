package com.mdex.venusAlpha01;

/**
 * Created by sehjin12-pc on 2016-12-11.
 */


import android.widget.Switch;

/**
 *
 * @details 압력세서로부터 전달 받은 데이터를 처리하여 직관적인 값으로 변환하고 사용할 변수에 Setting
 * @author Marveldex
 * @date 2017-03-17
 * @version 0.0.1
 * @li list1
 * @li list2
 *
 */

public class PacketParser {
    public static byte m_BatteryLevel = 0;
    public static byte def_CELL_COUNT_ROW0 = 16;
    public static byte def_CELL_COUNT_ROW1 = 16;
    public static byte def_CELL_COUNT_ROW2 = 10;


    public static byte [] nChairVal_Row0 = new byte [def_CELL_COUNT_ROW0];
    public static byte [] nChairVal_Row1 = new byte [def_CELL_COUNT_ROW1];
    public static byte [] nChairVal_Row2 = new byte [def_CELL_COUNT_ROW2];

    public static String Mode_Info = null;

    final byte [] packet_data_32bit = new byte[20];

    public static final byte [] adcValue_M = new byte[20]; // ADC of Main
    public static final byte [] adcValue_S = new byte[20]; // ADC of Shield

    public static final byte [] adcValue_L = new byte[20]; // ADC of Shield
    public static final byte [] adcValue_R = new byte[20]; // ADC of Shield

    public static boolean m_isPacketCompleted = false;

    public static String textHexaMain = " ";
    public static String textHexaShield = " ";


    public PacketParser(){
    }

    public static byte getSensorDataByCoord(int nRowIndex, int nColIndex){
        switch (nRowIndex){
            case 0:
                return nChairVal_Row0[nColIndex];
            case 1:
                return nChairVal_Row1[nColIndex];
            case 2:
                return nChairVal_Row2[nColIndex];
        }

        return 0;
    }

   /* public static byte getModeInfo(String mode_Info){

        if (mode_Info =='M'){

        }
        return 0;
    }*/

    /**
     *
     * @brief    전달 받은 압력 값을 구분하고 해당 값들을 변수에 Setting
     * @details sss
     * @param
     * @return
     * @throws
     */

    public void ParseOnePacket(byte [] packet_raw_data){
        for(int index = 0 ; index < 20 ; index++){
            packet_data_32bit[index] = packet_raw_data[index];// & 0xff;
        }

        int cell_index = 0;

        // select board m/s  - venus
        if( packet_data_32bit[0] == 'M'){
            textHexaMain = String.format("Ma: %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d",
                    packet_data_32bit[1], packet_data_32bit[2], packet_data_32bit[3], packet_data_32bit[4], packet_data_32bit[5], packet_data_32bit[6], packet_data_32bit[7], packet_data_32bit[8],
                    packet_data_32bit[9], packet_data_32bit[10], packet_data_32bit[11], packet_data_32bit[12], packet_data_32bit[13], packet_data_32bit[14], packet_data_32bit[15], packet_data_32bit[16]);

            System.arraycopy(packet_data_32bit, 1, adcValue_M, 0, 16); // 1: offset of source, 0: offset of dest, 16: length)

            m_isPacketCompleted = false;
            Mode_Info = "M";
        }
        else if( packet_data_32bit[0] == 'S') {
            textHexaShield = String.format("Sh: %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d",
                    packet_data_32bit[1], packet_data_32bit[2], packet_data_32bit[3], packet_data_32bit[4], packet_data_32bit[5], packet_data_32bit[6], packet_data_32bit[7], packet_data_32bit[8],
                    packet_data_32bit[9], packet_data_32bit[10], packet_data_32bit[11], packet_data_32bit[12], packet_data_32bit[13], packet_data_32bit[14], packet_data_32bit[15], packet_data_32bit[16]);
            System.arraycopy(packet_data_32bit, 1, adcValue_S, 0, 16); // 1: offset of source, 0: offset of dest, 16: length)

            m_isPacketCompleted = true;
            Mode_Info = "S";

         //l/r --> seat
        }else if(packet_data_32bit[0] == 'L') {

            textHexaMain = String.format("Ma: %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d",
                    packet_data_32bit[1], packet_data_32bit[2], packet_data_32bit[3], packet_data_32bit[4], packet_data_32bit[5], packet_data_32bit[6], packet_data_32bit[7], packet_data_32bit[8],
                    packet_data_32bit[9], packet_data_32bit[10], packet_data_32bit[11], packet_data_32bit[12], packet_data_32bit[13], packet_data_32bit[14], packet_data_32bit[15], packet_data_32bit[16]);

            System.arraycopy(packet_data_32bit, 1, adcValue_L, 0, 16); // 1: offset of source, 0: offset of dest, 16: length)

            m_isPacketCompleted = false;
            Mode_Info = "L";

        }else if(packet_data_32bit[0] == 'R') {

            textHexaShield = String.format("Sh: %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d",
                    packet_data_32bit[1], packet_data_32bit[2], packet_data_32bit[3], packet_data_32bit[4], packet_data_32bit[5], packet_data_32bit[6], packet_data_32bit[7], packet_data_32bit[8],
                    packet_data_32bit[9], packet_data_32bit[10], packet_data_32bit[11], packet_data_32bit[12], packet_data_32bit[13], packet_data_32bit[14], packet_data_32bit[15], packet_data_32bit[16]);
            System.arraycopy(packet_data_32bit, 1, adcValue_R, 0, 16); // 1: offset of source, 0: offset of dest, 16: length)

            m_isPacketCompleted = true;
            Mode_Info = "R";

        }

        //  assemble two packets into one.
        //  whole packet is 31byte long. It's divided 2 packets. 'M' is first, 'S' is last. So complete packet is assembled when 'S' is arrived.
        if( packet_data_32bit[0] == 'S') {
            cell_index = 0;

            nChairVal_Row0[cell_index++] = adcValue_M[0];
            nChairVal_Row0[cell_index++] = adcValue_M[1];
            nChairVal_Row0[cell_index++] = adcValue_M[2];
            nChairVal_Row0[cell_index++] = adcValue_M[3];
            nChairVal_Row0[cell_index++] = adcValue_M[4];
            nChairVal_Row0[cell_index++] = adcValue_M[5];
            nChairVal_Row0[cell_index++] = adcValue_M[6];
            nChairVal_Row0[cell_index++] = adcValue_M[7];
            nChairVal_Row0[cell_index++] = adcValue_M[8];
            nChairVal_Row0[cell_index++] = adcValue_M[9];
            nChairVal_Row0[cell_index++] = adcValue_M[10];
            nChairVal_Row0[cell_index++] = adcValue_M[11];
            nChairVal_Row0[cell_index++] = adcValue_M[12];
            nChairVal_Row0[cell_index++] = adcValue_M[13];
            nChairVal_Row0[cell_index++] = adcValue_M[14];
            nChairVal_Row0[cell_index++] = adcValue_M[15];



            cell_index = 0;
            nChairVal_Row1[cell_index++] = adcValue_S[0];
            nChairVal_Row1[cell_index++] = adcValue_S[1];
            nChairVal_Row1[cell_index++] = adcValue_S[2];
            nChairVal_Row1[cell_index++] = adcValue_S[3];
            nChairVal_Row1[cell_index++] = adcValue_S[4];
            nChairVal_Row1[cell_index++] = adcValue_S[5];
            nChairVal_Row1[cell_index++] = adcValue_S[6];
            nChairVal_Row1[cell_index++] = adcValue_S[7];
            nChairVal_Row1[cell_index++] = adcValue_S[8];
            nChairVal_Row1[cell_index++] = adcValue_S[9];
            nChairVal_Row1[cell_index++] = adcValue_S[10];
            nChairVal_Row1[cell_index++] = adcValue_S[11];
            nChairVal_Row1[cell_index++] = adcValue_S[12];
            nChairVal_Row1[cell_index++] = adcValue_S[13];
            nChairVal_Row1[cell_index++] = adcValue_S[14];
            nChairVal_Row1[cell_index++] = adcValue_S[15];

         }else if(packet_data_32bit[0] == 'R'){

            nChairVal_Row0[cell_index++] = adcValue_L[0];
            nChairVal_Row0[cell_index++] = adcValue_L[1];
            nChairVal_Row0[cell_index++] = adcValue_L[2];
            nChairVal_Row0[cell_index++] = adcValue_L[3];
            nChairVal_Row0[cell_index++] = adcValue_L[4];
            nChairVal_Row0[cell_index++] = adcValue_L[5];
            nChairVal_Row0[cell_index++] = adcValue_L[6];
            nChairVal_Row0[cell_index++] = adcValue_L[7];
            nChairVal_Row0[cell_index++] = adcValue_L[8];
            nChairVal_Row0[cell_index++] = adcValue_L[9];
            nChairVal_Row0[cell_index++] = adcValue_L[10];
            nChairVal_Row0[cell_index++] = adcValue_L[11];
            nChairVal_Row0[cell_index++] = adcValue_L[12];
            nChairVal_Row0[cell_index++] = adcValue_L[13];
            nChairVal_Row0[cell_index++] = adcValue_L[14];
            nChairVal_Row0[cell_index++] = adcValue_L[15];

            cell_index = 0;
            nChairVal_Row1[cell_index++] = adcValue_R[0];
            nChairVal_Row1[cell_index++] = adcValue_R[1];
            nChairVal_Row1[cell_index++] = adcValue_R[2];
            nChairVal_Row1[cell_index++] = adcValue_R[3];
            nChairVal_Row1[cell_index++] = adcValue_R[4];
            nChairVal_Row1[cell_index++] = adcValue_R[5];
            nChairVal_Row1[cell_index++] = adcValue_R[6];
            nChairVal_Row1[cell_index++] = adcValue_R[7];
            nChairVal_Row1[cell_index++] = adcValue_R[8];
            nChairVal_Row1[cell_index++] = adcValue_R[9];
            nChairVal_Row1[cell_index++] = adcValue_R[10];
            nChairVal_Row1[cell_index++] = adcValue_R[11];
            nChairVal_Row1[cell_index++] = adcValue_R[12];
            nChairVal_Row1[cell_index++] = adcValue_R[13];
            nChairVal_Row1[cell_index++] = adcValue_R[14];
            nChairVal_Row1[cell_index++] = adcValue_R[15];

        }

//      parse battery level
        {
            m_BatteryLevel = packet_data_32bit[17];
            String textHexa = String.format("Battery level : [%2d%%]", m_BatteryLevel);

            // in case of packet_data_32bit[17] is 8bit value of battery raw adc value.
            // int    BATTERY_VOLT_SHIFT = 250;
            // int    SCALE_OF_ADC = 100;
            // float  bat_volt = 0.0F;
            // bat_volt = (float)(packet_data_32bit[17] + BATTERY_VOLT_SHIFT) / SCALE_OF_ADC;
            // String textHexa = String.format("Device voltage : %1.2f V (%d)", bat_volt, packet_data_32bit[17]);
        }

    }

    public static boolean isPostureValid_Row1(){
        int summation = 0;
        for(int i = 0 ; i < def_CELL_COUNT_ROW1 ; i++) {
            summation += nChairVal_Row1[i];

            if(200 < summation) {
                return true;
            }
        }

        return false;
    }


    /**
     *
     * @brief
     * @details
     * @param
     * @return
     * @throws
     */

    public static float getCOM_x_Row1(){
        //  calculate COM - lateral
        int peak_value = 0;
        int peak_position = 0;
        int average_value = 0;
        int summation_value = 0;
        float level_pos = 0.0f;
        int pressure_new = 0;

        float position_weight = 0.0f; // 1.0f = pos of 1st dot, 0.0f = pos of 0th dot
        {
            for(int i = 0 ; i < def_CELL_COUNT_ROW1 ; i++) {
                if(peak_value < nChairVal_Row1[i]) {
                    peak_value = nChairVal_Row1[i];
                    peak_position = i;
                }

                if(0 < i) {
                    if( (nChairVal_Row1[i] == 0) && (summation_value == 0) ) {
                    }
                    else {
                        level_pos = i;
                        pressure_new = nChairVal_Row1[i];
                        position_weight = position_weight + (level_pos - position_weight) * pressure_new / (pressure_new + summation_value);
                    }
                }

                summation_value += nChairVal_Row1[i];

            }
            average_value = summation_value / def_CELL_COUNT_ROW1;

            String strText = String.format("Peek value : %d\nAverage value : %d\nCenter of pressure : %1.4f", peak_value, average_value, position_weight);
        }

        return position_weight;

        //  calculate COM - lean
        //  calculate COC - lateral
    }

    public static float getCOC_left_Row1() {
        int cell_index = 0;
        for(cell_index = 0 ; cell_index < def_CELL_COUNT_ROW1 ; cell_index++) {
            if(5 < nChairVal_Row1[cell_index]) {
                break;
            }
        }
        if(cell_index == def_CELL_COUNT_ROW1)
            cell_index = 0;

        //float coord_left = (float)cell_index - (float)def_CELL_COUNT_ROW1 / 2;
        float coord_left = (float)cell_index;

        return coord_left;
    }

    public static float getCOC_right_Row1() {
        int cell_index = 0;
        for(cell_index = (def_CELL_COUNT_ROW1 - 1) ; 0 <= cell_index ; cell_index--) {
            if(5 < nChairVal_Row1[cell_index]) {
                break;
            }
        }
        if(cell_index == 0)
            cell_index = def_CELL_COUNT_ROW1 - 1;

        //float coord_right = (float)cell_index - (float)def_CELL_COUNT_ROW1 / 2;
        float coord_right = (float)cell_index;

        return coord_right;
    }

    public static boolean isPacketCompleted(){
        return m_isPacketCompleted;
    }

    public static byte getBatteryLevel(){
        return m_BatteryLevel;
    }

    public static byte [] getDataMainBoard(){
        return adcValue_M;
    }

    public static byte [] getDataShieldBoard(){
        return adcValue_S;
    }

}
