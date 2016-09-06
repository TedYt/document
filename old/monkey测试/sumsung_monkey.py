from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
device = MonkeyRunner.waitForConnection()
#device.installPackage('workspace/Unlock/bin/Unlock.apk')
device.startActivity(component='com.android.contacts/.activities.DialtactsActivity',action='com.android.contacts.activities.DialtactsActivity')

def doit():
	mr=MonkeyRunner()
	mr.sleep(1)
	
	device.touch(360,1191,'DOWN_AND_UP')
	mr.sleep(1)
	device.touch(360,1191,'DOWN_AND_UP')
	mr.sleep(10)
	device.touch(597,897,'DOWN_AND_UP')	
	mr.sleep(7)

for number in range(1,600):
    doit()





#device.press('KEYCODE_MENU','DOWN_AND_UP')
#result = device.takeSnapShot
#result.writeToFile('monkeyrunnertest/shot1.png','png')
