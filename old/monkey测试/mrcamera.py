from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
device = MonkeyRunner.waitForConnection()
#device.installPackage('workspace/Unlock/bin/Unlock.apk')
def doit():
	device.startActivity(component='com.android.gallery3d/com.android.camera.Camera')
	mr=MonkeyRunner()
	mr.sleep(1)
#	device.shell('monkey -p com.android.camera.actor --ignore-crashes --ignore-timeouts --throttle 500 -v 20')
	mr.sleep(2)
	device.press('KEYCODE_BACK','DOWN_AND_UP')
#	device.press('KEYCODE_HOME','DOWN_AND_UP')
#	device.startActivity(component='com.android.launcher/com.android.launcher2.Launcher')
	mr.sleep(2)

for number in range(1,1000):
    doit()





#device.press('KEYCODE_MENU','DOWN_AND_UP')
#result = device.takeSnapShot
#result.writeToFile('monkeyrunnertest/shot1.png','png')
