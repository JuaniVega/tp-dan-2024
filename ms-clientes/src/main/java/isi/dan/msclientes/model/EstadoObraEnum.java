package isi.dan.msclientes.model;

public enum EstadoObraEnum {
	HABILITADA("Habilitada"),PENDIENTE("Pendiente"), FINALIZADA("Finalizada");
	
	private final String value;

	EstadoObraEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
