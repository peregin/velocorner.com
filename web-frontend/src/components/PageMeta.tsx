import { useEffect } from "react";

interface PageMetaProps {
  title: string;
  description?: string;
  canonical?: string;
}

const PageMeta = ({ title, description, canonical }: PageMetaProps) => {
  useEffect(() => {
    document.title = title;

    if (description) {
      const metaDesc = document.querySelector('meta[name="description"]');
      if (metaDesc) {
        metaDesc.setAttribute("content", description);
      }
    }
  }, [title, description]);

  return (
    <>
      <title>{title}</title>
      {description && <meta name="description" content={description} />}
      {canonical && <link rel="canonical" href={canonical} />}
    </>
  );
};

export default PageMeta;
