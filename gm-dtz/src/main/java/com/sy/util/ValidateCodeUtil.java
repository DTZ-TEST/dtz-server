package com.sy.util;

import com.sy.entity.message.CodeImage;
import com.sy.mainland.util.CommonUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.security.SecureRandom;

/**
 * 验证码
 * 
 * @author Administrator
 *
 */
public class ValidateCodeUtil {

	private static final char[] CODE_SEQUENCE1 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N','O',
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z','0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	private static final char[] CODE_SEQUENCE2 = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	private static final char[] CODE_SEQUENCE3 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N','O',
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

	public static final CodeImage createCode() throws Exception {
		String widthStr = PropUtil.getString("verification_image_width", "120", Constant.CORE_FILE);
		String heightStr = PropUtil.getString("verification_image_height", "40", Constant.CORE_FILE);
		String codeCountStr = PropUtil.getString("verification_image_code_count", "5", Constant.CORE_FILE);
		String lineCountStr = PropUtil.getString("verification_image_line_count", "100", Constant.CORE_FILE);

		int width = Integer.parseInt(widthStr);
		int height = Integer.parseInt(heightStr);
		int codeCount = Integer.parseInt(codeCountStr);
		int lineCount = Integer.parseInt(lineCountStr);

		int x = 0, fontHeight = 0, codeY = 0;
		int red = 0, green = 0, blue = 0;

		x = 3 * width / (4 * codeCount + 3);// width / (codeCount + 2);//
											// 每个字符的宽度
		fontHeight = height * 43 / 45;// 字体的高度
		codeY = height - (int) Math.floor(height * 7 / 45);// fontHeight +
															// ((height -
															// fontHeight) / 2);

		// 图像buffer
		BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = buffImg.createGraphics();
		// 生成随机数
		SecureRandom random = new SecureRandom();
		// 将图像填充颜色
		String rgba = PropUtil.getString("verification_code_color", "255,255,255,255", Constant.CORE_FILE);
		int rColor = 255, gColor = 255, bColor = 255, aColor = 255;
		rgba = CommonUtil.objectToString(rgba);
		if (rgba.length() > 0) {
			String[] strs = rgba.split(",");
			int len = strs.length;
			if (len > 0) {
				int temp = 255;
				if (CommonUtil.isNumeric(strs[0])) {
					temp = Integer.parseInt(strs[0]);
					if (temp < 255) {
						rColor = temp;
					}
				}

				if (len > 1) {
					if (CommonUtil.isNumeric(strs[1])) {
						temp = Integer.parseInt(strs[1]);
						if (temp < 255) {
							gColor = temp;
						}
					}

					if (len > 2) {
						if (CommonUtil.isNumeric(strs[2])) {
							temp = Integer.parseInt(strs[2]);
							if (temp < 255) {
								bColor = temp;
							}
						}

						if (len > 3) {
							if (CommonUtil.isNumeric(strs[3])) {
								temp = Integer.parseInt(strs[3]);
								if (temp < 255) {
									aColor = temp;
								}
							}
						}
					}
				}
			}
		}

		g.setColor(new Color(rColor, gColor, bColor, aColor));
		g.fillRect(0, 0, width, height);
		// 创建字体
		Font font = getFont(fontHeight);
		g.setFont(font);

		for (int i = 0; i < lineCount; i++) {
			int xs = random.nextInt(width);
			int ys = random.nextInt(height);
			int xe = xs + random.nextInt(width / 8);
			int ye = ys + random.nextInt(height / 8);
			red = random.nextInt(255);
			green = random.nextInt(255);
			blue = random.nextInt(255);
			g.setColor(new Color(red, green, blue));
			g.drawLine(xs, ys, xe, ye);
		}

		// randomCode记录随机产生的验证码
		StringBuilder randomCode = new StringBuilder(codeCount);
		// 随机产生codeCount个字符的验证码。

		char[] chars;
		String temp=PropUtil.getString("verification_image_code_type");
		if ("1".equals(temp)){
			chars=CODE_SEQUENCE1;
		}else if ("2".equals(temp)){
			chars=CODE_SEQUENCE2;
		}else{
			chars=CODE_SEQUENCE3;
		}

		for (int i = 0; i < codeCount; i++) {
			String strRand = String.valueOf(chars[random.nextInt(chars.length)]);
			// 产生随机的颜色值，让输出的每个字符的颜色值都将不同。
			red = random.nextInt(255);
			green = random.nextInt(255);
			blue = random.nextInt(255);
			g.setColor(new Color(red, green, blue));
			g.drawString(strRand, ((i + 1) * x / 3) + (i * x), codeY);
			// 将产生的四个随机数组合在一起。
			randomCode.append(strRand);
		}
		return new CodeImage(randomCode.toString(), buffImg);
	}

	public static final Font getFont(int fontHeight) {
		return new Font(PropUtil.getString("verification_code_font", "Arial", Constant.CORE_FILE), Font.PLAIN,
				fontHeight);
	}

	/**
	 * 输出图片
	 * 
	 * @param image
	 * @param outputStream
	 * @throws Exception
	 */
	public static final void write(BufferedImage image, OutputStream outputStream) throws Exception {
		ImageIO.write(image, "png", outputStream);
	}

}
