<%@ tag body-content="scriptless" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>${title}</title>
    <script src="https://cdn.tailwindcss.com?plugins=forms"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link
        href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700;800&family=Be+Vietnam+Pro:wght@400;500;600&display=swap"
        rel="stylesheet"
    />
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        appBg: "#f3f4ff",
                        appText: "#1f2440",
                        appPrimary: "#2f3f86",
                        appPrimarySoft: "#dde3ff",
                        appCard: "#ffffff",
                        appMuted: "#5b617c",
                        appBorder: "#d7d9ea",
                        appSurface: "#f6f7ff",
                        appSuccess: "#0f766e",
                        appSuccessSoft: "#ccfbf1",
                    },
                    fontFamily: {
                        headline: ["Plus Jakarta Sans", "sans-serif"],
                        body: ["Be Vietnam Pro", "sans-serif"],
                    },
                    boxShadow: {
                        soft: "0 24px 48px -32px rgba(37, 44, 89, 0.35)",
                    },
                },
            },
        };
    </script>
</head>
<body class="min-h-screen bg-appBg font-body text-appText antialiased">
    <div class="relative min-h-screen overflow-hidden">
        <jsp:doBody />
    </div>
</body>
</html>
