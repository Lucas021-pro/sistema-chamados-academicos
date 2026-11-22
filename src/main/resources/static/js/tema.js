(function aplicarTemaInicial() {
    const temaSalvo = localStorage.getItem('tema') || 'light';
    document.documentElement.setAttribute('data-theme', temaSalvo);
    document.documentElement.classList.toggle('dark', temaSalvo === 'dark');
})();

function alternarTema() {
    const atual = document.documentElement.classList.contains('dark') ? 'dark' : 'light';
    const proximo = atual === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', proximo);
    document.documentElement.classList.toggle('dark', proximo === 'dark');
    localStorage.setItem('tema', proximo);
}