/*
 * Copyright (c) 2011, Kustaa Nyholm / SpareTimeLabs
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list 
 * of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this 
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *  
 * Neither the name of the Kustaa Nyholm or SpareTimeLabs nor the names of its 
 * contributors may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package purejavacomm.testsuite;

import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;

public class Test5 extends TestBase {
	private static Exception m_Exception = null;
	private static Thread receiver;
	private static Thread transmitter;

	static void run() throws Exception {
		try {
			begin("Test5 - timeout");
			openPort();
			// receiving thread
			receiver = new Thread(new Runnable() {
				public void run() {
					try {
						sync(2);
						m_Port.enableReceiveThreshold(0);
						m_Port.enableReceiveTimeout(1000);
						long T0 = System.currentTimeMillis();
						byte[] b = { 0 };
						int n = m_In.read(b);
						long dT = System.currentTimeMillis() - T0;
						if (n != 0)
							fail("read did not time out as expected, read returned %d", n);
						if (dT < 1000)
							fail("-timed out early, expected 1000 msec, got %d msec", dT);
						if (dT > 1010)
							fail("read timed out with suspicious delay, expected 1000 msec, got %d msec", dT);
					} catch (InterruptedException e) {
					} catch (Exception e) {
						if (m_Exception == null)
							m_Exception = e;
						receiver.interrupt();
						transmitter.interrupt();
					}
				};
			});

			// sending thread
			transmitter = new Thread(new Runnable() {
				public void run() {
					try {
						sync(2);
					} catch (InterruptedException e) {
					} catch (Exception e) {
						e.printStackTrace();
						if (m_Exception == null)
							m_Exception = e;
						receiver.interrupt();
						transmitter.interrupt();
					}
				};
			});

			receiver.start();
			transmitter.start();

			while (receiver.isAlive() || transmitter.isAlive()) {
				sleep(100);
			}

			if (m_Exception != null)
				throw m_Exception;
			finishedOK();
		} finally {
			closePort();
		}

	}
}