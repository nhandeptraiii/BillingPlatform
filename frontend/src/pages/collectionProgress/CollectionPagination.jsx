import {
  useDispatch,
  useSelector
} from "react-redux";

import styles from "./CollectionPagination.module.scss";
import { selectPagination } from "../../redux/selector/collectionProgressSelector";
import { fetchCollectionProgress, setPage } from "../../redux/slices/collectionProgressSlice";


const CollectionPagination = () => {

  const dispatch =
    useDispatch();

  const {
    page,
    totalPages
  } = useSelector(
    selectPagination
  );

  const handleChangePage =
    (newPage) => {

      dispatch(
        setPage(newPage)
      );

      dispatch(
        fetchCollectionProgress()
      );
    };

  if (
    totalPages <= 1
  ) {
    return null;
  }

  const startPage =
    Math.max(
      0,
      page - 2
    );

  const endPage =
    Math.min(
      totalPages - 1,
      page + 2
    );

  const pageNumbers = [];

  for (
    let i = startPage;
    i <= endPage;
    i++
  ) {
    pageNumbers.push(i);
  }

  return (

    <div
      className={
        styles.pagination
      }
    >

      {/* First */}

      <button

        disabled={
          page === 0
        }

        onClick={() =>
          handleChangePage(0)
        }
      >
        ⏮
      </button>

      {/* Prev */}

      <button

        disabled={
          page === 0
        }

        onClick={() =>
          handleChangePage(
            page - 1
          )
        }
      >
        ◀
      </button>

      {pageNumbers.map(
        (item) => (

          <button

            key={item}

            className={
              page === item
                ? styles.active
                : ""
            }

            onClick={() =>
              handleChangePage(
                item
              )
            }
          >
            {item + 1}
          </button>

        )
      )}

      {/* Next */}

      <button

        disabled={
          page ===
          totalPages - 1
        }

        onClick={() =>
          handleChangePage(
            page + 1
          )
        }
      >
        ▶
      </button>

      {/* Last */}

      <button

        disabled={
          page ===
          totalPages - 1
        }

        onClick={() =>
          handleChangePage(
            totalPages - 1
          )
        }
      >
        ⏭
      </button>

      <div
        className={
          styles.summary
        }
      >
        Trang {page + 1} / {totalPages}
      </div>

    </div>
  );
};

export default CollectionPagination;