package com.sy.core;

import com.sy.mainland.util.core.AbstractPerformanceAOP;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 性能监视
 * 
 * @author Administrator
 *
 */
@Aspect
@Component
public class PerformanceAOP extends AbstractPerformanceAOP {

	@Override
	public boolean checkSwitch() {
		// TODO Auto-generated method stub
		return true;
	}
}
