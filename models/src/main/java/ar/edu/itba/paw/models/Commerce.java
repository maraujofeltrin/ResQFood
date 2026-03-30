package ar.edu.itba.paw.models;

public class Commerce {
	public enum Category {
		BAKERY,
		RESTAURANT,
		GREENGROCER,
		OTHER
	}

	private final Long userId;
	private String commercialName;
	private Category category;
	private String street;
	private Integer streetNumber;
	private String city;
	private String province;
	private String postalCode;
	private String openingTime;
	private String closingTime;

	public Commerce(Long userId, String commercialName, Category category, String street, Integer streetNumber,
			String city, String province, String postalCode, String openingTime, String closingTime) {
		this.userId = userId;
		this.commercialName = commercialName;
		this.category = category;
		this.street = street;
		this.streetNumber = streetNumber;
		this.city = city;
		this.province = province;
		this.postalCode = postalCode;
		this.openingTime = openingTime;
		this.closingTime = closingTime;
	}

	public Long getUserId() {
		return userId;
	}

	public String getCommercialName() {
		return commercialName;
	}

	public Category getCategory() {
		return category;
	}

	public String getStreet() {
		return street;
	}

	public Integer getStreetNumber() {
		return streetNumber;
	}

	public String getCity() {
		return city;
	}

	public String getProvince() {
		return province;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getOpeningTime() {
		return openingTime;
	}

	public String getClosingTime() {
		return closingTime;
	}

	@Override
	public String toString() {
		return "Commerce [userId=" + userId + ", commercialName=" + commercialName + ", category=" + category
				+ ", street=" + street + ", streetNumber=" + streetNumber + ", city=" + city + ", province="
				+ province + ", postalCode=" + postalCode + ", openingTime=" + openingTime + ", closingTime="
				+ closingTime + "]";
	}
}
