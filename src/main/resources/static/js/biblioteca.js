"use strict";

const tabs = [...document.querySelectorAll('[role="tab"]')];
const feedback = document.getElementById("form-feedback");
const note = document.getElementById("form-note");

function mostrarMensagem(mensagem, erro = false) {
  feedback.textContent = mensagem;
  feedback.classList.toggle("feedback-error", erro);
  feedback.hidden = false;
}

function selecionarFormulario(tab) {
  tabs.forEach((item) => {
    const selecionado = item === tab;
    item.setAttribute("aria-selected", String(selecionado));
    item.tabIndex = selecionado ? 0 : -1;
    item.classList.toggle("active", selecionado);
    document.getElementById(item.getAttribute("aria-controls")).hidden = !selecionado;
  });
  note.textContent = tab.id === "tab-cadastro"
    ? "Crie sua conta para começar. A entrada na biblioteca estará disponível em breve."
    : "Prévia do login: nenhum dado deste formulário será enviado ou salvo.";
  feedback.hidden = true;
}

tabs.forEach((tab, index) => {
  tab.addEventListener("click", () => selecionarFormulario(tab));
  tab.addEventListener("keydown", (event) => {
    let next;
    if (event.key === "ArrowRight") next = tabs[(index + 1) % tabs.length];
    if (event.key === "ArrowLeft") next = tabs[(index - 1 + tabs.length) % tabs.length];
    if (event.key === "Home") next = tabs[0];
    if (event.key === "End") next = tabs[tabs.length - 1];
    if (!next || next.disabled) return;
    event.preventDefault();
    selecionarFormulario(next);
    next.focus();
  });
});

document.querySelectorAll("[data-password]").forEach((button) => {
  button.addEventListener("click", () => {
    const input = document.getElementById(button.dataset.password);
    const mostrar = input.type === "password";
    input.type = mostrar ? "text" : "password";
    button.setAttribute("aria-label", mostrar ? "Ocultar senha" : "Mostrar senha");
    button.setAttribute("aria-pressed", String(mostrar));
  });
});

const senha = document.getElementById("cadastro-senha");
const confirmacao = document.getElementById("cadastro-confirmacao");
const nome = document.getElementById("cadastro-nome");

function validarConfirmacao() {
  const diferente = confirmacao.value !== "" && confirmacao.value !== senha.value;
  confirmacao.setCustomValidity(diferente ? "As senhas precisam ser iguais." : "");
}

senha.addEventListener("input", validarConfirmacao);
confirmacao.addEventListener("input", validarConfirmacao);
nome.addEventListener("input", () => nome.setCustomValidity(nome.value.trim() ? "" : "Informe seu nome."));

function limparFormulario(form) {
  form.reset();
  validarConfirmacao();
  form.querySelectorAll("[data-password]").forEach((button) => {
    document.getElementById(button.dataset.password).type = "password";
    button.setAttribute("aria-label", "Mostrar senha");
    button.setAttribute("aria-pressed", "false");
  });
}

const cadastro = document.getElementById("form-cadastro");
const submitCadastro = cadastro.querySelector('[type="submit"]');
let cadastrando = false;

cadastro.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (cadastrando || !cadastro.reportValidity()) return;
  cadastrando = true;
  submitCadastro.disabled = true;
  submitCadastro.textContent = "Criando sua conta…";
  cadastro.setAttribute("aria-busy", "true");
  tabs.forEach(tab => { tab.disabled = true; });
  feedback.hidden = true;

  try {
    // O token vincula o POST à sessão deste navegador, mantendo CSRF ativo.
    const tokenResponse = await fetch("/api/csrf", { credentials: "same-origin", cache: "no-store" });
    if (!tokenResponse.ok) throw new Error("Token indisponível");
    const csrf = await tokenResponse.json();
    const response = await fetch("/api/usuarios", {
      method: "POST",
      credentials: "same-origin",
      headers: { "Content-Type": "application/json", [csrf.headerName]: csrf.token },
      body: JSON.stringify({ nome: nome.value, email: document.getElementById("cadastro-email").value, senha: senha.value })
    });

    if (response.status === 201) {
      limparFormulario(cadastro);
      mostrarMensagem("Conta criada! Seus dados foram salvos. O login estará disponível na próxima etapa.");
      return;
    }
    if (response.status === 403) {
      mostrarMensagem("Sua sessão expirou. Tente enviar o formulário novamente.", true);
      return;
    }
    if (response.status === 400 || response.status === 409) {
      const erro = await response.json();
      const campos = Object.values(erro.campos ?? {}).join(" ");
      mostrarMensagem(campos || erro.mensagem || "Confira os dados e tente novamente.", true);
      return;
    }
    mostrarMensagem("Não foi possível concluir o cadastro agora. Tente novamente.", true);
  } catch {
    mostrarMensagem("Não foi possível confirmar o cadastro. Confira sua conexão e tente novamente.", true);
  } finally {
    cadastrando = false;
    submitCadastro.disabled = false;
    submitCadastro.textContent = "Criar minha conta ↗";
    cadastro.removeAttribute("aria-busy");
    tabs.forEach(tab => { tab.disabled = false; });
  }
});

const login = document.getElementById("form-login");
login.addEventListener("submit", (event) => {
  event.preventDefault();
  if (!login.reportValidity()) return;
  limparFormulario(login);
  mostrarMensagem("Esta é uma prévia: nenhum login foi realizado e nenhum dado foi enviado. A autenticação será conectada na próxima etapa.");
});

document.querySelectorAll("form").forEach((form) => {
  // Habilita somente depois de impedir o envio nativo para outra página.
  form.querySelector('[type="submit"]').disabled = false;
  form.addEventListener("input", () => { feedback.hidden = true; });
});
