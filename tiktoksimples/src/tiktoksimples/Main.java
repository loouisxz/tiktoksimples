package tiktoksimples;

import java.util.*;

// Representa um usuário do app
class User {
    private String name;
    private int id;
    private Set<User> followings; // Usuários que este usuário segue

    public User(String name, int id) {
        this.name = name;
        this.id = id;
        this.followings = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void follow(User user) {
        if (user != null && user != this) {
            followings.add(user);
        }
    }

    public Set<User> getFollowings() {
        return Collections.unmodifiableSet(followings);
    }
}

// Representa um vídeo postado por um usuário
class Video {
    private int id;
    private String title;
    private User owner;
    private Map<String, Set<User>> reactions; // tipo de reação -> usuários que reagiram
    private List<String> comments;

    public Video(int id, String title, User owner) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.reactions = new HashMap<>();
        this.comments = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public User getOwner() {
        return owner;
    }

    public void addReaction(String type, User user) {
        reactions.putIfAbsent(type, new HashSet<>());
        reactions.get(type).add(user);
    }

    public void addComment(String comment) {
        comments.add(comment);
    }

    public Map<String, Set<User>> getReactions() {
        return reactions;
    }

    public List<String> getComments() {
        return comments;
    }
}

// Interface para serviços que adicionam reação num vídeo
interface ReactionService {
    String getReactionType();
    void react(Video video, User user);
}

// Serviço para curtida "gostei"
class LikeService implements ReactionService {
    public String getReactionType() {
        return "gostei";
    }

    public void react(Video video, User user) {
        video.addReaction(getReactionType(), user);
    }
}

// Serviço para comentário
class CommentService {
    public void comment(Video video, String comment) {
        video.addComment(comment);
    }
}

// Serviço para gerenciar vídeos e feed
class FeedService {
    private List<Video> videos;

    public FeedService() {
        videos = new ArrayList<>();
    }

    public void postVideo(Video video) {
        videos.add(video);
    }

    // Retorna vídeos do usuário e que seus seguidos postaram
    public List<Video> getFeed(User user) {
        Set<User> followedUsers = user.getFollowings();
        List<Video> feed = new ArrayList<>();

        for (Video v : videos) {
            if (v.getOwner().equals(user) || followedUsers.contains(v.getOwner())) {
                feed.add(v);
            }
        }

        return feed;
    }

    // Mostrar feed simples
    public void showFeed(User user) {
        List<Video> feed = getFeed(user);

        if (feed.isEmpty()) {
            System.out.println("Seu feed está vazio.");
            return;
        }

        System.out.println("Feed de " + user.getName() + ":");

        for (Video v : feed) {
            System.out.println("ID: " + v.getId() + " - Título: " + v.getTitle() + " - Dono: " + v.getOwner().getName());
            System.out.print("Reações: ");

            if (v.getReactions().isEmpty()) {
                System.out.print("Nenhuma");
            } else {
                for (Map.Entry<String, Set<User>> entry : v.getReactions().entrySet()) {
                    System.out.print(entry.getKey() + "(" + entry.getValue().size() + ") ");
                }
            }
            System.out.println();

            System.out.println("Comentários:");
            if (v.getComments().isEmpty()) {
                System.out.println("Nenhum comentário");
            } else {
                for (String c : v.getComments()) {
                    System.out.println("- " + c);
                }
            }

            System.out.println("-------------------");
        }
    }
}

// Programa principal para usar o app
public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static Map<Integer, User> users = new HashMap<>();
    private static FeedService feedService = new FeedService();
    private static CommentService commentService = new CommentService();
    private static LikeService likeService = new LikeService();

    private static int nextUserId = 1;
    private static int nextVideoId = 1;

    public static void main(String[] args) {
        System.out.println("Bem-vindo ao TikTok Simplificado!");
        boolean running = true;

        while (running) {
            System.out.println("\nMenu:");
            System.out.println("1 - Criar usuário");
            System.out.println("2 - Postar vídeo");
            System.out.println("3 - Curtir vídeo");
            System.out.println("4 - Comentar vídeo");
            System.out.println("5 - Seguir usuário");
            System.out.println("6 - Ver feed");
            System.out.println("7 - Sair");
            System.out.print("Escolha: ");

            int choice = lerInt();

            switch (choice) {
                case 1 -> criarUsuario();
                case 2 -> postarVideo();
                case 3 -> curtirVideo();
                case 4 -> comentarVideo();
                case 5 -> seguirUsuario();
                case 6 -> mostrarFeed();
                case 7 -> {
                    System.out.println("Saindo...");
                    running = false;
                }
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    private static int lerInt() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            return -1;
        }
    }

    private static void criarUsuario() {
        System.out.print("Nome do usuário: ");
        String name = scanner.nextLine();
        User user = new User(name, nextUserId++);
        users.put(user.getId(), user);
        System.out.println("Usuário criado com ID " + user.getId());
    }

    private static User escolherUsuario() {
        if (users.isEmpty()) {
            System.out.println("Nenhum usuário cadastrado.");
            return null;
        }
        System.out.println("Usuários:");
        for (User user : users.values()) {
            System.out.println(user.getId() + " - " + user.getName());
        }
        System.out.print("Digite o ID do usuário: ");
        int id = lerInt();
        User user = users.get(id);
        if (user == null) {
            System.out.println("Usuário não encontrado.");
        }
        return user;
    }

    private static void postarVideo() {
        User user = escolherUsuario();
        if (user == null) return;

        System.out.print("Título do vídeo: ");
        String title = scanner.nextLine();
        Video video = new Video(nextVideoId++, title, user);
        feedService.postVideo(video);
        System.out.println("Vídeo postado!");
    }

    private static void curtirVideo() {
        User user = escolherUsuario();
        if (user == null) return;

        List<Video> feed = feedService.getFeed(user);
        if (feed.isEmpty()) {
            System.out.println("Nenhum vídeo para curtir no seu feed.");
            return;
        }

        System.out.println("Vídeos disponíveis:");
        for (Video v : feed) {
            System.out.println(v.getId() + " - " + v.getTitle() + " (dono: " + v.getOwner().getName() + ")");
        }
        System.out.print("Digite o ID do vídeo para curtir: ");
        int vid = lerInt();

        Video video = null;
        for (Video v : feed) {
            if (v.getId() == vid) {
                video = v;
                break;
            }
        }

        if (video == null) {
            System.out.println("Vídeo não encontrado no seu feed.");
            return;
        }

        likeService.react(video, user);
        System.out.println("Você curtiu o vídeo!");
    }

    private static void comentarVideo() {
        User user = escolherUsuario();
        if (user == null) return;

        List<Video> feed = feedService.getFeed(user);
        if (feed.isEmpty()) {
            System.out.println("Nenhum vídeo no seu feed para comentar.");
            return;
        }

        System.out.println("Vídeos disponíveis:");
        for (Video v : feed) {
            System.out.println(v.getId() + " - " + v.getTitle() + " (dono: " + v.getOwner().getName() + ")");
        }
        System.out.print("Digite o ID do vídeo para comentar: ");
        int vid = lerInt();

        Video video = null;
        for (Video v : feed) {
            if (v.getId() == vid) {
                video = v;
                break;
            }
        }

        if (video == null) {
            System.out.println("Vídeo não encontrado no seu feed.");
            return;
        }

        System.out.print("Comentário: ");
        String comment = scanner.nextLine();
        commentService.comment(video, user.getName() + ": " + comment);
        System.out.println("Comentário adicionado.");
    }

    private static void seguirUsuario() {
        User user = escolherUsuario();
        if (user == null) return;

        System.out.println("Usuários disponíveis para seguir:");
        for (User u : users.values()) {
            if (!u.equals(user)) {
                System.out.println(u.getId() + " - " + u.getName());
            }
        }
        System.out.print("Digite o ID do usuário para seguir: ");
        int id = lerInt();

        User toFollow = users.get(id);
        if (toFollow == null || toFollow.equals(user)) {
            System.out.println("Usuário inválido para seguir.");
            return;
        }

        user.follow(toFollow);
        System.out.println("Você está seguindo " + toFollow.getName());
    }

    private static void mostrarFeed() {
        User user = escolherUsuario();
        if (user == null) return;

        feedService.showFeed(user);
    }
}
