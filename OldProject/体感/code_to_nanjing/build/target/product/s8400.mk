
PRODUCT_PACKAGES := \
#    FMRadio
#    MyTube \
#    VideoPlayer

PRODUCT_PACKAGES += \
    libmfvfactory\
    eemcs_mdinit\
    eemcs_fsd\
    GesturePhone\
	eemcs_fsvc\
	ObjectRemover

$(call inherit-product, $(SRC_TARGET_DIR)/product/common.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/telephony.mk)

# Overrides
PRODUCT_BRAND  := SUGAR
PRODUCT_MANUFACTURER := SUGAR
PRODUCT_MODEL  := SS119
PRODUCT_NAME   := $(TARGET_PRODUCT)
PRODUCT_DEVICE := $(TARGET_PRODUCT)

#third apks
$(call inherit-product, vendor/tinno/prebuilt-apps/cmcc_apk/s8400_chinamobile.mk)

# cinemagraph libs
PRODUCT_COPY_FILES += vendor/tinno/s8400/etc/cinemagraph_lib/libmorpho_cinema_graph.so:system/lib/libmorpho_cinema_graph.so
PRODUCT_COPY_FILES += vendor/tinno/s8400/etc/cinemagraph_lib/libmorpho_jpeg_io.so:system/lib/libmorpho_jpeg_io.so
PRODUCT_COPY_FILES += vendor/tinno/s8400/etc/cinemagraph_lib/libmorpho_memory_allocator.so:system/lib/libmorpho_memory_allocator.so
PRODUCT_COPY_FILES += vendor/tinno/s8400/etc/objecteraser_lib/libmorpho_object_remover_jni.so:system/lib/libmorpho_object_remover_jni.so
