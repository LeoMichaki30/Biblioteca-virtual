"use strict";

// A prévia trabalha apenas com o DOM: não faz requisições nem salva dados.
const tabs = [...document.querySelectorAll('[role="tab"]')];
const feedback = document.getElementById("form-feedback");

function selecionarFormulario(tab) {
  tabs.forEach((item) => {
    const selecionado = item === tab;
    item.setAttribute("aria-selected", String(selecionado));
    item.tabIndex = selecionado ? 0 : -1;
    item.classList.toggle("active", selecionado);
    document.getElementById(item.getAttribute("aria-controls")).hidden = !selecionado;
  });
  feedback.hidden = true;
  feedback.textContent = "";
}

tabs.forEach((tab, index) => {
  tab.addEventListener("click", () => selecionarFormulario(tab));
  tab.addEventListener("keydown", (event) => {
    let next;
    if (event.key === "ArrowRight") next = tabs[(index + 1) % tabs.length];
    if (event.key === "ArrowLeft") next = tabs[(index - 1 + tabs.length) % tabs.length];
    if (event.key === "Home") next = tabs[0];
    if (event.key === "End") next = tabs[tabs.length - 1];
    if (!next) return;
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

function validarConfirmacao() {
  const diferente = confirmacao.value !== "" && confirmacao.value !== senha.value;
  confirmacao.setCustomValidity(diferente ? "As senhas precisam ser iguais." : "");
}

senha.addEventListener("input", validarConfirmacao);
confirmacao.addEventListener("input", validarConfirmacao);

document.querySelectorAll("form").forEach((form) => {
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    if (!form.reportValidity()) return;
    const cadastro = form.id === "form-cadastro";
    feedback.textContent = cadastro
      ? "Formulário validado! Esta é uma prévia: nenhuma conta foi criada e nenhum dado foi enviado. O cadastro real será conectado em uma próxima etapa."
      : "Formulário validado! Esta é uma prévia: nenhum login foi realizado e nenhum dado foi enviado. A autenticação será conectada em uma próxima etapa.";
    feedback.hidden = false;
    form.reset();
    validarConfirmacao();
    form.querySelectorAll('[data-password]').forEach((button) => {
      document.getElementById(button.dataset.password).type = "password";
      button.setAttribute("aria-label", "Mostrar senha");
      button.setAttribute("aria-pressed", "false");
    });
  });
  // Só habilita o envio depois de instalar o listener que impede navegação.
  form.querySelector('[type="submit"]').disabled = false;
  form.addEventListener("input", () => { feedback.hidden = true; });
});
