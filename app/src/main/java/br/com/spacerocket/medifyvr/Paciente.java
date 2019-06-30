package br.com.spacerocket.medifyvr;

public class Paciente {
    private String telefone;
    private String idMedico;
    private String nome;

    public Paciente() {}

    public Paciente(String telefone, String idMedico, String nome) {
        this.telefone = telefone;
        this.idMedico = idMedico;
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(String idMedico) {
        this.idMedico = idMedico;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
