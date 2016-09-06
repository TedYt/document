#!/bin/bash

# PROJECT
PROJECT=tinnoes17_s9050

SRC_DIR=$PWD
DEST_DIR=$PWD

DIRNAME='tinnoPackage';
DIRDATABASENAME='database';

#modem db path
MDDBPATH=mediatek/custom/common/modem/tinnoes17_s9050_gprs_sp/BPLGUInfoCustomApp_MT6577_S00_11A_MD_W12_18_P2
#AP db path
APDBPATH=mediatek/source/cgen/APDB_MT6577_S01_ALPS.ICS_

if [ -d $DIRNAME ]
then
	echo "  Start copy [ ${DIRNAME} ] ..."
else
{
	mkdir ${DIRNAME}
	mkdir -p ${DIRNAME}/${DIRDATABASENAME}
	echo "  Start copy [ ${DIRNAME} ] ..."
}
fi

rm ${DEST_DIR}/${DIRNAME}/*
rm ${DEST_DIR}/${DIRNAME}/${DIRDATABASENAME}}/*

#database
cp ${SRC_DIR}/${MDDBPATH}      ${DEST_DIR}/${DIRNAME}/${DIRDATABASENAME}/${PROJECT}_modemdatabase
cp ${SRC_DIR}/${APDBPATH} 	   ${DEST_DIR}/${DIRNAME}/${DIRDATABASENAME}/${PROJECT}_apdatabaese

cp ${SRC_DIR}/mediatek/source/misc/MT6577_Android_scatter_emmc.txt 		${DEST_DIR}/${DIRNAME}/MT6577_Android_scatter_emmc.txt

cp ${SRC_DIR}/out/target/product/${PROJECT}/DSP_BL 		${DEST_DIR}/${DIRNAME}/DSP_BL

cp ${SRC_DIR}/out/target/product/${PROJECT}/EBR1 		${DEST_DIR}/${DIRNAME}/EBR1
cp ${SRC_DIR}/out/target/product/${PROJECT}/EBR2 		${DEST_DIR}/${DIRNAME}/EBR2
cp ${SRC_DIR}/out/target/product/${PROJECT}/MBR 		${DEST_DIR}/${DIRNAME}/MBR
cp ${SRC_DIR}/out/target/product/${PROJECT}/cache.img 		${DEST_DIR}/${DIRNAME}/cache.img

cp ${SRC_DIR}/out/target/product/${PROJECT}/system.img 		${DEST_DIR}/${DIRNAME}/system.img
cp ${SRC_DIR}/out/target/product/${PROJECT}/secro.img 		${DEST_DIR}/${DIRNAME}/secro.img
cp ${SRC_DIR}/out/target/product/${PROJECT}/recovery.img 	${DEST_DIR}/${DIRNAME}/recovery.img
cp ${SRC_DIR}/out/target/product/${PROJECT}/boot.img 		${DEST_DIR}/${DIRNAME}/boot.img
cp ${SRC_DIR}/out/target/product/${PROJECT}/userdata.img 	${DEST_DIR}/${DIRNAME}/userdata.img
#cp ${SRC_DIR}/mediatek/source/preloader/preloader_${PROJECT}.bin ${DEST_DIR}/${DIRNAME}/preloader_${PROJECT}.bin
cp ${SRC_DIR}/mediatek/source/preloader/preloader_${PROJECT}.bin ${DEST_DIR}/${DIRNAME}/preloader.bin
#cp ${SRC_DIR}/bootable/bootloader/uboot/uboot_${PROJECT}.bin 	${DEST_DIR}/${DIRNAME}/uboot_${PROJECT}.bin
cp ${SRC_DIR}/bootable/bootloader/uboot/uboot_${PROJECT}.bin 	${DEST_DIR}/${DIRNAME}/uboot.bin
cp ${SRC_DIR}/bootable/bootloader/uboot/logo.bin 			${DEST_DIR}/${DIRNAME}/logo.bin

echo "  Successful copy to [ ${DIRNAME} ] !"


