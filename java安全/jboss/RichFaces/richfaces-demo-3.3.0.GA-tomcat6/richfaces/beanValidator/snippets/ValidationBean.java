/**
 * 
 */
package org.richfaces.demo.validation;

import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.Max;
import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

/**
 * @author Ilya Shaikovsky
 *
 */
public class ValidationBean {

	private String progressString="Fill the form please";
	
	@NotEmpty
	@Length(min=3,max=12)
	private String name;
	@Email
	@NotEmpty
	private String email;
	@NotNull
	@Min(18)
	@Max(100)
	private int age;
	
	public ValidationBean() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	public void success() {
		setProgressString(getProgressString() + "(Strored successfully)");
	}

	public String getProgressString() {
		return progressString;
	}

	public void setProgressString(String progressString) {
		this.progressString = progressString;
	}
}
