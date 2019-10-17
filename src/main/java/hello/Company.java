package hello;

public class Company {
	private String name;
	private long employees;
	private String headoffice;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getEmployees() {
		return employees;
	}
	public void setEmployees(Long employees) {
		this.employees = employees;
	}
	public String getHeadoffice() {
		return headoffice;
	}
	public void setHeadoffice(String headoffice) {
		this.headoffice = headoffice;
	}

	@Override
	public String toString() {
		return "The company name: " + this.getName() + ", Employees count: " + this.getEmployees() + ", Headoffice: " + this.getHeadoffice();
	}
}
