package br.com.spacerocket.medifyvr;

public class Paciente {
    private String genero;
    private String data_nascimento;
    private String idMedico;
    private String nome;

    public Paciente() {}

    public Paciente(String genero, String data_nascimento, String idMedico, String nome) {
        this.genero = genero;
        this.data_nascimento = data_nascimento;
        this.idMedico = idMedico;
        this.nome = nome;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getData_nascimento() {
        return data_nascimento;
    }

    public void setData_nascimento(String data_nascimento) {
        this.data_nascimento = data_nascimento;
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
